(ns board-ultimatum.engine.neural
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config])
  (:use clojure.pprint)
  (use [enclog nnets training])
  (:use [monger.operators]))

; lein exec -p src/board_ultimatum/engine/neural.clj

(let [connection-info (if (nil? (:db-name config/storage))
     (assoc config/storage :db-name "board_ultimatum") config/storage)]

  (try
    (model/connect connection-info)
    (catch java.io.IOException e
      (println "ERROR: Could not connect to MongoDB."))
    (catch java.lang.NullPointerException e
      (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
               (str (assoc connection-info :password "********"))))))

;; network functions

(def net
  (network  (neural-pattern :feed-forward)
    :activation :sigmoid
    :input   20
    :output  1
    :hidden [20 10]))

(def training-set (data :basic-dataset [[0.0 0.0 0.0 0.0 0.0
                                         0.0 0.0 0.0 0.0 0.0
                                         0.0 0.0 0.0 0.0 0.0
                                         0.0 0.0 0.0 0.0 0.0]]
                                       [[1.0]]))

(def dummy-input (data :basic-dataset [[0.0 0.0 0.0 0.0 0.0
                                       0.0 0.0 0.0 0.0 0.0
                                       0.0 0.0 0.0 0.0 0.0
                                       0.0 0.0 0.0 0.0 0.0]
                                      [1.0 1.0 1.0 1.0 1.0
                                       1.0 1.0 1.0 1.0 1.0
                                       1.0 1.0 1.0 1.0 1.0
                                       1.0 1.0 1.0 1.0 1.0]]
                                      [[-1.0]
                                       [-1.0]]))

(def prop-train (trainer :resilient-prop :network net :training-set training-set)) 

;; for each id, join each id with every other id and calculate output

(def game-ids
  (into [] 
    (map 
      (fn [game] (:bgg_id game))
      (model/find-all))))

(defn get-vector [game-id] 
  (:data (mc/find-one-as-map "network_data" {:id 299})))

(defn join-vector [id-A id-B] (into [] (concat (get-vector id-A) (get-vector id-B))))

(defn to-dataset [id-A id-B] (data :basic-dataset [(join-vector id-A id-B)]
                                [[-1.0]]))

(defn output-pair [id-A id-B] (map 
  (fn [pair] (. (. net compute (. pair getInput)) getData 0))
  (to-dataset id-A id-B)))

;; iterate and add top 50 games to DB

(pprint (:data (mc/find-one-as-map "network_data" {:id 299})))
(pprint (get-vector 299))

(mc/remove "network_output")

(doseq [id-A game-ids] 
  (doseq [game-record (take 50 
    (sort-by :rating >
      (map 
        (fn [id-B] 
          { :rating (nth (output-pair id-A id-B) 0) :game_a id-A :game_b id-B })
        game-ids)))]
    (mc/insert "network_output" game-record)))

