(ns board-ultimatum.engine.neural
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.model.relationship :as relationship]
            [board-ultimatum.engine.config :as config])
  (:use clojure.pprint)
  (:use clojure.math.numeric-tower)
  (use [enclog nnets training])
  (:use [monger.operators]))

(:import 
  org.encog.neural.networks.training.cross.CrossValidationKFold
  org.encog.ml.data.folded.FoldedDataSet)

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
    :input   4
    :output  1
    :hidden [4 4]))

;; for each id, join each id with every other id and calculate output

(def game-ids
  (into [] 
    (map 
      (fn [game] (:bgg_id game))
      (model/find-all))))

(defn get-vector [game-id] 
  (:data (mc/find-one-as-map "network_data" {:id game-id})))

(defn join-vector [id-A id-B] 
  "Concatenate two vectors"
  (into [] (concat (get-vector id-A) (get-vector id-B))))

(defn vector-abs-difference [id-A id-B]
  "calculate the absoulute value difference between the two vectors" 
  (into [] 
    (map
      (fn [a b] 
        (abs (- a b))) 
      (get-vector id-A) 
      (get-vector id-B))))

(defn to-dataset [id-A id-B] (data :basic-dataset [(join-vector id-A id-B)]
                                [[-1.0]]))

(defn output-pair [id-A id-B] (map 
  (fn [pair] (. (. net compute (. pair getInput)) getData 0))
  (to-dataset id-A id-B)))

;; set up the training data

(def training-set 
  (new org.encog.ml.data.folded.FoldedDataSet 
    (data :basic-dataset 
      (into [] 
        (map 
          (fn [rel] 
            (join-vector (nth (:_id rel) 0) (nth (:_id rel) 1)))
          (relationship/average-ratings)))
      (into [] 
        (map 
          (fn [rel] 
            [(:rating rel)])
          (relationship/average-ratings))))))

(def prop-train (trainer :resilient-prop :network net :training-set training-set)) 

;; use this function to train the network

(defn train-network []
  (train prop-train 0.0001 500 []))

;; train with cross validation

(def cross-trainer
  (new org.encog.neural.networks.training.cross.CrossValidationKFold prop-train 5))

(defn cross-iteration [] (.iteration cross-trainer))

(defn cross-error [] (.getError cross-trainer))

(defn cross-train []
  (loop []
    (do 
        (cross-iteration)
        (println (cross-error)) 
        (if (<= (cross-error) 0.001)
          (cross-error)
          (recur)))))

;; iterate and add top 50 games to DB

(defn network-eval [] 
  (dorun 
    (mc/remove "network_output_ml")
    (doseq [id-A game-ids] 
      (doseq [game-record (take 50 
        (sort-by :rating >
          (map 
            (fn [id-B] 
              { :rating (nth (output-pair id-A id-B) 0) :game_a id-A :game_b id-B })
            game-ids)))]
        (mc/insert "network_output_ml" game-record)))))

