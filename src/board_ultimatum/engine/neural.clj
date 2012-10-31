(ns board-ultimatum.engine.neural
    (:require [taoensso.carmine :as car])
    (:use clojure.pprint)
    (use [enclog nnets training]))

; lein exec -p src/board_ultimatum/engine/neural.clj

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

(def input (data :basic-dataset [[0.0 0.0 0.0 0.0 0.0
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

;; establish redis connections
;; db 0 => list of vector values keyed on game id
;; db 1 => sorted sets keyed on game id where weight is output and member is second game

(def pool (car/make-conn-pool))

(defn spec-server [db-num] (car/make-conn-spec :db db-num))

(defmacro wcar0 [& body] `(car/with-conn pool (spec-server 0) ~@body))
(defmacro wcar1 [& body] `(car/with-conn pool (spec-server 1) ~@body))

(pprint (wcar0 (car/ping)))
(pprint (wcar1 (car/ping)))

(doseq [pair input] 
  (let [output (. net compute (. pair getInput ))] 
  (println "Output = " (. output getData  0))))

