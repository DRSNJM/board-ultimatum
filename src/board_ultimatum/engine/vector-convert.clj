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

(model/find-all)