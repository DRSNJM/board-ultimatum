(ns board-ultimatum.engine.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.tag :as tag])
  (:use [monger.operators]
        [clojure.pprint]
        [clojure.string :only [blank?]]))

;; This namespace contains all functions related to manipulating the
;; applications "model" (which is mostly mongo).

(defn ensure-indexes
  "Ensures the existence of several indexes to use mongo efficiently."
  []
  (mc/ensure-index "experts" {:identifier 1})
  (mc/ensure-index "board_games" {:bgg_id 1} {:unique 1 :dropDups 1})
  (mc/ensure-index "board_games" {:name 1})
  (mc/ensure-index "board_games" {:bgg_id 1, :random "2d"})
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
  (mc/find-maps "board_games"))

(defn tag-values-by-subtype [game subtype]
  (set (map :value (filter #(= subtype (:subtype %)) (:tags game)))))

(defn mechanics [game]
  (tag-values-by-subtype game "mechanic"))

(defn categories [game]
  (tag-values-by-subtype game "category"))


(def basic-score-weight 100)

(defn score-attr [subtype [attr-name influence-sign]]
  (let [tag (mc/find-one-as-map "tags" {:subtype (tag/singular-subtype subtype) :value attr-name})]
    (if (pos? influence-sign)
      {:score (Integer/parseInt (:pos-influence tag)) :reason (:value tag)}
      {:score (Integer/parseInt (:neg-influence tag)) :reason (:value tag)})))

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

(defn num-players-factors [game attrs]
  (let [players (:num-players attrs)]
    (if (> (count players) 0)
      {:reason "Optimal Player Number"
       :score (num-players-score players game)})))

(defn weight-factor [game attrs]
  (if (not (clojure.string/blank? (:weight attrs)))
    (let [w (:weight_average game)
          x (. Float parseFloat (:weight attrs))
          d (- x w)
          score (- 100 (* 16 d d))]
      {:reason
        (cond
          (> score 80.0) "Close Weight"
          (> score 0.0) "Acceptable Weight"
          (> w x) "Weight Too High"
          :else "Weight Too Low")
       :score score})))

;; returns [ [attr-name value] ]
(defn score-factor [attr-type game relevant-attrs]
  (let [game-attrs (tag-values-by-subtype game (tag/singular-subtype attr-type))]
    (remove nil?
            (map #(if (contains? game-attrs (str (first %)))
                    (score-attr attr-type %))
                 relevant-attrs))))

(defn collect-score-factors [game query-attrs]
  (flatten
   (remove empty?
           (list
            (score-factor "mechanics" game (:mechanics query-attrs))
            (score-factor "categories" game (:categories query-attrs))
            (rank-score game)
            (num-players-factors game query-attrs)
            (weight-factor game query-attrs)))))

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

;; functions for getting similar game results

(defn get-game-by-name [name]
  "Get a game from the db by name"
  (mc/find-one-as-map "board_games" {:name name}))

(defn get-id-by-name [name]
  "Returns the id of the game with the provided name"
  (:bgg_id (get-game-by-name name)))
                                   
(defn get-similar [id]
  "Get the ids of all games similar to that provided"
  (mc/find-maps "network_output" {:game_a id}))
