(ns board-ultimatum.engine.neural
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config]
            [taoensso.carmine :as car])
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

;(mc/insert "network_output" { :first_name "John" :last_name "Lennon" })

;; establish redis connections
;; db 0 => list of vector values keyed on game id
;; db 1 => training data
;;         sorted sets keyed on game id where weight is output and member is second game
;; db 2 => output data
;;         sorted sets keyed on game id where weight is output and member is second game

(def pool (car/make-conn-pool))

(defn spec-server [db-num] (car/make-conn-spec :db db-num))

(defmacro wcar0 [& body] `(car/with-conn pool (spec-server 0) ~@body))
(defmacro wcar1 [& body] `(car/with-conn pool (spec-server 1) ~@body))
(defmacro wcar2 [& body] `(car/with-conn pool (spec-server 2) ~@body))

(pprint (wcar0 (car/ping)))

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

;; for each id, join each id with every other id

(def game-ids
  (into [] 
    (map 
      (fn [game] (:bgg_id game))
      (model/find-all))))

(defn get-vector [game-id] 
  (into [] 
    (map 
      (fn [n] (read-string n)) 
      (wcar0 (car/lrange game-id 0 10)))))

(defn join-vector [id-A id-B] (into [] (concat (get-vector id-A) (get-vector id-B))))

(defn to-dataset [id-A id-B] (data :basic-dataset [(join-vector id-A id-B)]
                                [[-1.0]]))

(defn add-result-redis [id-A id-B weight] (wcar2 
  (car/zadd id-A weight id-B)))

(defn add-result-mongo [id-A id-B weight]
  (mc/insert "network_output" { :game_a id-A :game_b id-B :rating weight }))

(defn output-pair [id-A id-B] (map 
  (fn [pair] (. (. net compute (. pair getInput)) getData 0))
  (to-dataset id-A id-B)))

;; iterate and add top 50 games to DB
; (mc/insert "network_output"

(doseq [id-A game-ids] 
  (doseq [game-record (take 50 
    (sort-by :rating >
      (map 
        (fn [id-B] 
          { :rating (nth (output-pair id-A id-B) 0) :game_a id-A :game_b id-B })
        game-ids)))]
    (mc/insert "network_output" game-record)))

