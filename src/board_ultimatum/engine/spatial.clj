(ns board-ultimatum.engine.spatial
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.model.relationship :as relationship]
            [board-ultimatum.engine.config :as config])
  (:use clojure.pprint)
  (:use clojure.math.numeric-tower)
  (:use [monger.operators]))

(let [connection-info (if (nil? (:db-name config/storage))
     (assoc config/storage :db-name "board_ultimatum") config/storage)]

  (try
    (model/connect connection-info)
    (catch java.io.IOException e
      (println "ERROR: Could not connect to MongoDB."))
    (catch java.lang.NullPointerException e
      (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
               (str (assoc connection-info :password "********"))))))

;; 

(def game-ids
  (into [] 
    (map 
      (fn [game] (:bgg_id game))
      (model/find-all))))

(defn get-vector [game-id] 
  (:data (mc/find-one-as-map "network_data" {:id game-id})))


(defn euc-dist [vec-a vec-b] 
  "Returns the euclidean distance between 2 vectors"
  (sqrt
    (apply +
      (map
        (fn [a b]
          (expt (- a b) 2))
        vec-a
        vec-b)))) 

(defn calc-rating [id-A id-B]
  (if (= id-A id-B) 
    0.0
    (/ (- 4.0 (euc-dist (get-vector id-A) (get-vector id-B))) 4.0)))

;; iterate and add top 30 games to DB

(defn data-eval [] 
  (dorun 
    (mc/remove "network_output")
    (doseq [id-A game-ids] 
      (doseq [game-record (take 30 
        (sort-by :rating >
          (map 
            (fn [id-B] 
              { :rating (calc-rating id-A id-B) :game_a id-A :game_b id-B })
            game-ids)))]
        (mc/insert "network_output" game-record)))))

(defn init-adjusted-data []
  (doseq [id game-ids]
    (mc/update "adjusted_output" 
      { :id id}
      { :id id :data (get-vector id) }
      :upsert true )))

(defn spatial-shift []
  (doseq [rel (relationship/average-ratings)]
    (doseq [id game-ids]
      (mc/update "adjusted_output" 
        { :id id}
        { :id id :data [0.0 0.0] }
        :upsert true ))))

