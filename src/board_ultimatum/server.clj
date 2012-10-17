(ns board-ultimatum.server
  (:require [noir.server :as server]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config]
            [monger.core :as mg])
  (:import [com.mongodb MongoOptions ServerAddress]))

(server/load-views-ns 'board-ultimatum.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'board-ultimatum})))

