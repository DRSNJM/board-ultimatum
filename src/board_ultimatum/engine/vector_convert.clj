(ns board-ultimatum.engine.vector-convert
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config])
  (:use clojure.pprint)
  (:use clojure.set)
  (:use incanter.core incanter.stats incanter.charts))

(let [connection-info (if (nil? (:db-name config/storage))
     (assoc config/storage :db-name "board_ultimatum") config/storage)]

  (try
    (model/connect connection-info)
    (catch java.io.IOException e
      (println "ERROR: Could not connect to MongoDB."))
    (catch java.lang.NullPointerException e
      (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
               (str (assoc connection-info :password "********"))))))

; Vector format:
; 0 - game length
; 1 - min players
; 2 - max players
; 3 - min age
; 4 - rank
; 5 - weight
; 6 - rating
; 7 - rank
; 8... categories/mechanics

(defn has-tag [game subtype value]
  (cond
    (some #(= value %) 
      (remove nil? 
        (map (fn [tag] 
          (cond (= (:subtype tag) subtype) (:value tag))) (:tags game)))) 1.0
     :else 0.0))

(defn game-categories [game]
  "Return all categories for some game"
  (into #{} (remove nil? 
    (map (fn [tag] 
      (cond (= (:subtype tag) "category") (:value tag))) (:tags game)))))

(defn game-mechanics [game]
  "Return all game mechanics for some game"
  (into #{} (remove nil? 
    (map (fn [tag] 
      (cond (= (:subtype tag) "mechanic") (:value tag))) (:tags game)))))

(defn all-categories []
  "Return all possible categories"
  (seq (apply union (map game-categories
    (model/find-all)))))

(defn all-mechanics []
  "Return all possible mechanics"
  (seq (apply union (map game-mechanics
    (model/find-all)))))

(defn to-vector [game]
  "Convert Mongo game record to normalized numeric vector"
  { :id (:bgg_id game)
    :data [ (/ (:length game) 6000.0)
            (/ (:min_players game) 8.0)
            (/ (:max_players game) 8.0)
            (/ (:min_age game) 18.0)
            (/ (:rank game) 1000.0)
            (/ (:weight_average game) 5.0)
            (/ (:rating_average game) 10.0)
            (/ (:rank game) 1000.0)
            (has-tag game "category" "Video Game Theme")
            (has-tag game "category" "Vietnam War")
            (has-tag game "category" "Aviation / Flight")
            (has-tag game "category" "Print & Play")
            (has-tag game "category" "Memory")
            (has-tag game "category" "Negotiation")
            (has-tag game "category" "Novel-based")
            (has-tag game "category" "Card Game")
            (has-tag game "category" "Prehistoric")
            (has-tag game "category" "Movies / TV / Radio theme")
            (has-tag game "category" "Exploration")
            (has-tag game "category" "Trivia")
            (has-tag game "category" "World War I")
            (has-tag game "category" "Humor")
            (has-tag game "category" "Arabian")
            (has-tag game "category" "Mythology")
            (has-tag game "category" "Napoleonic")
            (has-tag game "category" "Deduction")
            (has-tag game "category" "World War II")
            (has-tag game "category" "Fantasy")
            (has-tag game "category" "American Revolutionary War")
            (has-tag game "category" "Children's Game")
            (has-tag game "category" "Dice")
            (has-tag game "category" "Space Exploration")
            (has-tag game "category" "City Building")
            (has-tag game "category" "Action / Dexterity")
            (has-tag game "category" "Book")
            (has-tag game "category" "Spies/Secret Agents")
            (has-tag game "category" "Horror")
            (has-tag game "category" "Mafia")
            (has-tag game "category" "Word")
            (has-tag game "category" "Industry / Manufacturing")
            (has-tag game "category" "Trains")
            (has-tag game "category" "Party Game")
            (has-tag game "category" "Transportation")
            (has-tag game "category" "Korean War")
            (has-tag game "category" "Farming")
            (has-tag game "category" "Zombies")
            (has-tag game "category" "Racing")
            (has-tag game "category" "American West")
            (has-tag game "category" "Adventure")
            (has-tag game "category" "Abstract Strategy")
            (has-tag game "category" "Mature / Adult")
            (has-tag game "category" "Medical")
            (has-tag game "category" "Civilization")
            (has-tag game "category" "Fighting")
            (has-tag game "category" "Renaissance")
            (has-tag game "category" "Educational")
            (has-tag game "category" "American Indian Wars")
            (has-tag game "category" "Economic")
            (has-tag game "category" "Miniatures")
            (has-tag game "category" "Modern Warfare")
            (has-tag game "category" "Comic Book / Strip")
            (has-tag game "category" "Bluffing")
            (has-tag game "category" "Nautical")
            (has-tag game "category" "Animals")
            (has-tag game "category" "Murder/Mystery")
            (has-tag game "category" "Science Fiction")
            (has-tag game "category" "Ancient")
            (has-tag game "category" "Medieval")
            (has-tag game "category" "Territory Building")
            (has-tag game "category" "Pirates")
            (has-tag game "category" "Collectible Components")
            (has-tag game "category" "Wargame")
            (has-tag game "category" "Travel")
            (has-tag game "category" "Religious")
            (has-tag game "category" "Civil War")
            (has-tag game "category" "American Civil War")
            (has-tag game "category" "Real-time")
            (has-tag game "category" "Electronic")
            (has-tag game "category" "Game System")
            (has-tag game "category" "Political")
            (has-tag game "category" "Puzzle")
            (has-tag game "category" "Environmental")
            (has-tag game "category" "Sports")
            (has-tag game "category" "Maze")
            (has-tag game "mechanic" "Rock-Paper-Scissors")
            (has-tag game "mechanic" "Trading")
            (has-tag game "mechanic" "Memory")
            (has-tag game "mechanic" "Press Your Luck")
            (has-tag game "mechanic" "Area Movement")
            (has-tag game "mechanic" "Worker Placement")
            (has-tag game "mechanic" "Pick-up and Deliver")
            (has-tag game "mechanic" "Crayon Rail System")
            (has-tag game "mechanic" "Paper-and-Pencil")
            (has-tag game "mechanic" "Pattern Building")
            (has-tag game "mechanic" "Role Playing")
            (has-tag game "mechanic" "Roll / Spin and Move")
            (has-tag game "mechanic" "Variable Phase Order")
            (has-tag game "mechanic" "Voting")
            (has-tag game "mechanic" "Simulation")
            (has-tag game "mechanic" "Pattern Recognition")
            (has-tag game "mechanic" "Simultaneous Action Selection")
            (has-tag game "mechanic" "Betting/Wagering")
            (has-tag game "mechanic" "Grid Movement")
            (has-tag game "mechanic" "Variable Player Powers")
            (has-tag game "mechanic" "Area Control / Area Influence")
            (has-tag game "mechanic" "Dice Rolling")
            (has-tag game "mechanic" "Route/Network Building")
            (has-tag game "mechanic" "Campaign / Battle Card Driven")
            (has-tag game "mechanic" "Chit-Pull System")
            (has-tag game "mechanic" "Partnerships")
            (has-tag game "mechanic" "Auction/Bidding")
            (has-tag game "mechanic" "Commodity Speculation")
            (has-tag game "mechanic" "Modular Board")
            (has-tag game "mechanic" "Acting")
            (has-tag game "mechanic" "Deck / Pool Building")
            (has-tag game "mechanic" "Action Point Allowance System")
            (has-tag game "mechanic" "Secret Unit Deployment")
            (has-tag game "mechanic" "Card Drafting")
            (has-tag game "mechanic" "Line Drawing")
            (has-tag game "mechanic" "Tile Placement")
            (has-tag game "mechanic" "Hex-and-Counter")
            (has-tag game "mechanic" "Hand Management")
            (has-tag game "mechanic" "Point to Point Movement")
            (has-tag game "mechanic" "Area-Impulse")
            (has-tag game "mechanic" "Time Track")
            (has-tag game "mechanic" "Stock Holding")
            (has-tag game "mechanic" "Storytelling")
            (has-tag game "mechanic" "Set Collection")
            (has-tag game "mechanic" "Co-operative Play")
            (has-tag game "mechanic" "Trick-taking")
            (has-tag game "mechanic" "Area Enclosure")
          ]})

;; compile full vector data into a matrix

(def game-ids
  (into [] 
    (map 
      (fn [game] (:bgg_id game))
      (model/find-all))))

(def full-data (matrix (into [] (map 
  (fn [game] (:data game)) 
  (map to-vector
    (model/find-all))))))

(defn data-convert-no-pca [] 
  (dorun 
    ;; add the data to the mongo db
    (mc/remove "network_data")
    (dorun (map 
        (fn [id data] 
          (mc/insert "network_data" { :id id :data (into [] data) }))
        game-ids 
        full-data)
    )))

;; perform pca on the data

(def pca (principal-components full-data))

(def components (:rotation pca))

(defn pc [n]
    (into [] (map 
        (fn [i] 
          (sel components :cols i))
        (range n))))

(defn pca-x [n]
    (into [] (map 
        (fn [i] 
          (mmult full-data (nth (pc n) i)))
        (range n))))

(defn data-convert [n] 
  (dorun 
    ;; add the data to the mongo db
    (mc/remove "network_data")
    (dorun 
      (map 
        (fn [id data] 
          (mc/insert "network_data" { :id id :data (into [] data) }))
        game-ids 
        (trans (matrix (pca-x n)))))))

(def data-2d (dataset 
  ["id" "x1" "x2"]
  (trans (matrix [game-ids (nth (pca-x 2) 0) (nth (pca-x 2) 1)]))))

(defn view-2d []
    "Plot the data in 2D"
    (do 
        (view (scatter-plot (nth (pca-x 2) 0) (nth (pca-x 2) 1) 
                            :x-label "PC1" 
                            :y-label "PC2" 
                            :title "Game Data"))
        ;; view a table of the dataset
        (view ($order [:x1 :x2] :desc data-2d))))