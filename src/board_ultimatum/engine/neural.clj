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


(doseq [pair input] 
  (let [output (. net compute (. pair getInput ))] 
  (println "Output = " (. output getData  0))))

