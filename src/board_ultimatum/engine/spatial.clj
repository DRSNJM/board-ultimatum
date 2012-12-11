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

(defn get-adjusted [game-id] 
  (:data (mc/find-one-as-map "adjusted_output" {:id game-id})))


(defn euc-dist [vec-a vec-b] 
  "Returns the euclidean distance between 2 vectors"
  (sqrt
    (apply +
      (map
        (fn [a b]
          (expt (- a b) 2))
        vec-a
        vec-b))))

(defn vector-difference [vec-a vec-b] 
  "Returns the differnce between 2 vectors"
  (into []
    (map
      (fn [a b]
        (- a b))
      vec-a
      vec-b)))  

(defn calc-rating [id-A id-B]
  (if (= id-A id-B) 
    0.0
    (/ (- 10.0 (euc-dist (get-adjusted id-A) (get-adjusted id-B))) 10.0)))

;; iterate and add top 30 games to DB

(defn data-eval [] 
  (dorun 
    (mc/remove "network_output_stats")
    (doseq [id-A game-ids] 
      (doseq [game-record (take 30 
        (sort-by :rating >
          (map 
            (fn [id-B] 
              { :rating (calc-rating id-A id-B) :game_a id-A :game_b id-B })
            game-ids)))]
        (mc/insert "network_output_stats" game-record)))))

(defn init-adjusted-data []
  "Set up the spatial data"
  (doseq [id game-ids]
    (mc/update "adjusted_output" 
      { :id id}
      { :id id :data (get-vector id) }
      :upsert true )))

(defn gauss-dist [vec-a vec-b]
  (expt 2.187 
        (* -2.0 
           (expt (euc-dist vec-a vec-b) 
                 2))))

(defn adjust-point [data-point point-a point-b weight]
  "Adjust the location of a certain datapoint based on an existing relationship"
  (into [] 
    (map
      (fn [x a b]
        (+ x
           (* (- weight 0.5)
              (+ (* (/ (- b a) 
                       (euc-dist point-a point-b))
                    (gauss-dist data-point point-a))  
                 (* (/ (- a b) 
                       (euc-dist point-b point-a))
                    (gauss-dist data-point point-b))))))
      data-point 
      point-a
      point-b)))

(defn spatial-shift []
  "Shift the data based on the relationships in the db and save the results"
  (doseq [rel (relationship/average-ratings)]
    (doseq [id game-ids]
      (mc/update "adjusted_output" 
        { :id id}
        { :id id :data (adjust-point (get-adjusted id) 
                                     (get-adjusted (nth (:_id rel) 0))
                                     (get-adjusted (nth (:_id rel) 1))
                                     (:rating rel))}
        :upsert true ))))

