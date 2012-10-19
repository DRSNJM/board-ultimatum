(ns board-ultimatum.script.vector
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config])
  (:use clojure.pprint))

; run this script via:
; lein exec -p src/board_ultimatum/engine/vector-convert.clj

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

(defn to-vector [game]
  "Convert Mongo game record to normalized numeric vector"
  (vector (/ (:length game) 6000.0)
          (/ (:min_players game) 8.0)
          (/ (:max_players game) 8.0)
          (/ (:min_age game) 18.0)
          (/ (:rank game) 1000.0)
          (/ (:weight_num game) 2000.0)
          ))

(pprint (map to-vector
  (model/find-all)))