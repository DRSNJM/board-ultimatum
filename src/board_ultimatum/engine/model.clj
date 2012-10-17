(ns board-ultimatum.engine.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [clojure.xml :as xml])
  (:use monger.operators))

;; This namespace contains all functions related to manipulating the
;; applications "model" (which is mostly mongo).

(defn ensure-indexes
  "Ensures the existence of several indexes to use mongo effectively."
  []
  (mc/ensure-index "games" {:id 1})
  (mc/ensure-index "games" {:name 1}))

(defn connect
  "Connect to mongo based on the given connection information."
  [connection-info]
  (if (:uri connection-info)
    (mg/connect-via-uri! (:uri connection-info))
    (let [db-name (:db-name connection-info)]
      (mg/connect!)
      (when (not (nil? (:username connection-info)))
        (mg/authenticate db-name
                         (:username connection-info)
                         (into-array Character/TYPE (:password connection-info))))
      (mg/set-db! (mg/get-db db-name))))
  ; Set up the indexes necessary for decent performance.
  (ensure-indexes))

(defn add-games
  "Batch inserts the given sequence of games into mongo."
  [games]
  (mc/insert-batch "games" games))
