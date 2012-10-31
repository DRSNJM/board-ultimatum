(ns board-ultimatum.engine.model
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.attr_engine :as attr_engine])
  (:use [monger.operators]))

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
                         (into-array Character/TYPE
                                     (:password connection-info))))
      (mg/set-db! (mg/get-db db-name))))
  ; Set up the indexes necessary for decent performance.
  (ensure-indexes))

(def time-map
  {20 [10 15 20]
   30 [25 30 35]
   45 [40 45 50]
   60 [45 50 60 70 75]
   90 [75 80 90 100]
   120 [100 120 135]
   180 [150 180 200]
   240 [210 240]
   300 [300]
   360 [420 480 600 720 1200 6000]})

(defn times [selected]
  "Turns user inputted time approx. ranges into database queries matching the
  actual game lengths in the database.

  Usage: (times [30 45]) => (25 30 35 40 45 50)"
  (mapcat #(time-map %) selected))

(defn filter-on-times [selected-times attrs games]
  (if (and (> (count selected-times) 0) (Boolean/valueOf (:length-active attrs)))
    (filter #(boolean (some #{(:length %)} (times selected-times))) games)
    games))

(defn filter-on-num-players [selected-num-players attrs games]
  ;(println (:tags (first games)))
  (if (and (> (count selected-num-players) 0) (Boolean/valueOf (:num-players-active attrs)))
    (let [min-pl (apply min selected-num-players)
          max-pl (apply max selected-num-players)]
      (filter #(not (or (> min-pl (:max_players %))
                        (< max-pl (:min_players %)))) games))
    games))

(defn find-games [selected-times selected-num-players attrs]
    "Queries mongo for games matching selected inputs."
    (let [collection "board_games"]
      (let [games (mc/find-maps collection)]
        (attr_engine/score-games attrs
          (filter-on-times selected-times attrs 
            (filter-on-num-players selected-num-players attrs games))))))