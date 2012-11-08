(ns board-ultimatum.engine.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.attr_engine :as attr_engine])
  (:use [monger.operators]
        [clojure.pprint]))

;; This namespace contains all functions related to manipulating the
;; applications "model" (which is mostly mongo).

(defn ensure-indexes
  "Ensures the existence of several indexes to use mongo efficiently."
  []
  (mc/ensure-index "experts" {:identifier 1})
  (mc/ensure-index "board_games" {:bgg_id 1} {:unique 1 :dropDups 1})
  (mc/ensure-index "board_games" {:name 1})
  (mc/ensure-index "board_games" {:bgg_id 1, :random "2d"}))
  (mc/ensure-index "network_data" {:id 1})
  (mc/ensure-index "network_output" {:game_a 1}))


(defn connect
  "Connect to mongo based on the given connection information."
  [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (let [db-name (:db-name connection-info)]
      (mg/connect!)
      (when (not (nil? (:username connection-info)))
        (mg/authenticate db-name
                         (:username connection-info)
                         (into-array Character/TYPE
                                     (:password connection-info))))
      (mg/set-db! (mg/get-db db-name))))
  ; Set up the indexes necessary for decent performance.
  (ensure-indexes))

(def time-map
  {20 [10 15 20]
   30 [25 30 35]
   45 [40 45 50]
   60 [45 50 60 70 75]
   90 [75 80 90 100]
   120 [100 120 135]
   180 [150 180 200]
   240 [210 240]
   300 [300]
   360 [420 480 600 720 1200 6000]})

(defn times [selected]
  "Turns user inputted time approx. ranges into database queries matching the
  actual game lengths in the database.

  Usage: (times [30 45]) => (25 30 35 40 45 50)"
  (mapcat #(time-map %) selected))
  
(defn find-all []
  "Queries mongo for all games."
  (let [collection "board_games"]
    (mc/find-maps collection {})))

; These suck. Pulls all the data from mongo, and then filters. For
; each. Every time. Super dumb.
(defn uniq-tag [subtype]
  (distinct
   (map #(:value %)
        (filter
         (fn [h] (= (:subtype h) subtype))
         (flatten
          (map (fn [g] (:tags g))
               (mc/find-maps "board_games")))))))

(defn all-mechanics []
  (uniq-tag "mechanic"))

(defn all-categories []
  (uniq-tag "category"))

(defn all-families []
  (uniq-tag "family"))

(defn all-publishers []
  (uniq-tag "publisher"))

(defn all-designers []
  (uniq-tag "designer"))



(defn tag-values-by-subtype [game subtype]
  (set (map :value (filter #(= subtype (:subtype %)) (:tags game)))))

(defn mechanics [game]
  (tag-values-by-subtype game "mechanic"))

(defn categories [game]
  (tag-values-by-subtype game "category"))



;; extract all this nonsense and store it in the databse

(def most-popular-categories
 (list "Card Game" "Wargame" "Economic" "Fantasy" "Fighting" "Medieval" "Ancient" "Science Fiction" "World War II" "Adventure" "City Building" "Bluffing" "Exploration" "Political" "Miniatures" "Negotiation" "Civilization" "Abstract Strategy" "Transportation" "Territory Building" "Deduction" "Dice" "Nautical" "Racing" "Trains" "Animals" "Novel-based" "Horror" "Party Game" "Renaissance" "Humor" "Industry / Manufacturing" "Aviation / Flight" "Sports" "Action / Dexterity" "Mythology" "Space Exploration" "Travel" "Farming" "Napoleonic" "Movies / TV / Radio theme" "Real-time" "Murder/Mystery" "Children's Game" "American West" "Collectible Components" "Puzzle"))

(def most-popular-mechanics
  (list "Hand Management" "Dice Rolling" "Variable Player Powers" "Area Control / Area Influence" "Set Collection" "Modular Board" "Auction/Bidding" "Tile Placement" "Card Drafting" "Simultaneous Action Selection" "Area Movement" "Action Point Allowance System" "Route/Network Building" "Hex-and-Counter" "Partnerships" "Point to Point Movement" "Simulation" "Campaign / Battle Card Driven" "Worker Placement" "Secret Unit Deployment" "Pick-up and Deliver" "Co-operative Play" "Trading" "Role Playing" "Commodity Speculation" "Variable Phase Order" "Stock Holding" "Voting" "Betting/Wagering" "Grid Movement" "Deck / Pool Building" "Area Enclosure" "Roll / Spin and Move" "Memory" "Pattern Building" "Press Your Luck" "Trick-taking" "Chit-Pull System" "Pattern Recognition" "Storytelling"))

(def basic-score-weight 100)

(defn simple-score-data [data]
  (reduce #(assoc %1 %2 {:score basic-score-weight :reason %2}) {} data))

(def simple-mechanics-score-data
  (simple-score-data most-popular-mechanics))

(def simple-category-score-data
  (simple-score-data most-popular-categories))

;; silly, quick hack
(defn simple-data [attr-type]
  (cond
   (= "mechanics" attr-type) simple-mechanics-score-data
   (= "categories" attr-type) simple-category-score-data))

(defn singular-attr [attr]
  (if (= attr "mechanics")
    "mechanic"
    "category"))

(defn score-attr [score-data [attr-name attr-value]]
  (let [new-score (* (:score (score-data attr-name))
                     attr-value)]
    (assoc (score-data attr-name) :score new-score)))

(defn rank-score [game]
  {:reason "BGG Rank"
   :score (- basic-score-weight (/ (float (:rank game)) 10))})

;; player num

(defn convert-player-num [num-key]
  (if (< (.indexOf (name num-key) "+") 0)
    (Integer/parseInt (name num-key))
    100))

(defn recommended-num-player-votes [hsh [player-num poll-result]]
  (assoc hsh (convert-player-num player-num)
    (+
     (* 2 (poll-result :Best))
     (* 1 (poll-result :Recommended))
     (* -1 (poll-result (keyword "Not Recommended"))))))

(defn tally-player-poll [game]
  (reduce #(recommended-num-player-votes %1 %2) {}
          (game :suggested_players)))

(defn max-kv-by-value [hsh]
  (reduce (fn [[maxk maxv] [k v]]
            (if (> v maxv)
              [k v]
              [maxk maxv]))
          [-1 -100]
          hsh))

(defn normalize-votes [optimal-num votes]
  (reduce (fn [hsh [p v]]
            (assoc hsh p (/ (float v) (get votes optimal-num))))
          {}
          votes))

(defn optimal-player-num [game]
  (let [player-votes (tally-player-poll game)]
    (first (max-kv-by-value player-votes))))

(defn num-players-score [players game]
  (let [min-pl (apply min players)
        max-pl (apply max players)
        player-votes (tally-player-poll game)
        optimal-num (first (max-kv-by-value player-votes))
        norm-votes (normalize-votes optimal-num player-votes)]
    (cond
     (contains? (set players) optimal-num) basic-score-weight
     (> optimal-num max-pl) (if (contains? (set (keys norm-votes)) max-pl)
                              (* (get norm-votes max-pl) basic-score-weight)
                              (* -1 basic-score-weight))
     (< optimal-num min-pl) (if (contains? (set (keys norm-votes)) min-pl)
                              (* (get norm-votes min-pl) basic-score-weight)
                              (* -1 basic-score-weight))
     :else 0)))

(defn num-players-factors [attrs game]
  (let [players (:num-players attrs)]
    (if (> (count players) 0)
      {:reason "Optimal Player Number"
       :score (num-players-score players game)})))

;; player num

;; returns [ [attr-name value] ]
(defn score-factor [attr-type game query-attrs]
  (let [relevant-attrs (query-attrs (keyword attr-type))
        game-attrs (tag-values-by-subtype game (singular-attr attr-type))
        score-data (simple-data attr-type)]
    (remove nil?
            (map #(if (contains? game-attrs (str (first %)))
                     (score-attr score-data %))
                 relevant-attrs))))

(defn collect-score-factors [game query-attrs]
  (flatten
   (remove empty?
           (list
            (score-factor "mechanics" game query-attrs)
            (score-factor "categories" game query-attrs)
            (rank-score game)
            (num-players-factors query-attrs game)))))

(defn sum-score [factors]
  (apply + (map :score factors)))

(defn total-score [game]
  (sum-score (:factors game)))

(defn sorted-ranked-games [games query-attrs]
  (take 30
        (sort-by
         #(* -1 (:score %))
         (filter #(> (:score %) 0)
                 (map #(let [factors (collect-score-factors % query-attrs)]
                         (assoc % :factors factors
                                :score (sum-score factors)))
                      games)))))


(defn filter-on-times [attrs games]
  (let [selected-times (:length attrs)]
    (if (> (count selected-times) 0)
      (filter #(boolean (some #{(:length %)} (times selected-times))) games)
      games)))

(defn filter-on-num-players [attrs games]
  (let [selected-num-players (:num-players attrs)]
    (if (> (count selected-num-players) 0)
      (let [min-pl (apply min selected-num-players)
            max-pl (apply max selected-num-players)]
        (filter #(not (or (> min-pl (:max_players %))
                          (< max-pl (:min_players %)))) games))
      games)))


(defn find-games [query-attrs]
    "Queries mongo for games matching selected inputs."
    (let [collection "board_games"
          games (mc/find-maps collection)]
      (sorted-ranked-games
       (->> games
            (filter-on-times query-attrs)
            (filter-on-num-players query-attrs))
       query-attrs)))

(defn get-game-by-id
  "Get an expert from the database by id."
  ([id fields] (mc/find-one-as-map "board_games"
                                   {:bgg_id id}
                                   fields))
  ([id] (get-game-by-id id [])))

(defn add-random-field-to-games
  "This function should only be run as a cron task. It adds/update a random
  field on each game in the board-games collection.

  NOTE: This may be improved by using mongo's built-in map-reduce functionality.
  Also, there is a race condition between when each game document is fetched and
  updates start."
  []
  (map (fn [{obj-id :_id}]
         (mc/update-by-id "board_games" obj-id {$set {:random [(rand) 0]}}))
       (mc/find-maps "board_games" {} [:_id])))
