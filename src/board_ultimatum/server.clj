(ns board-ultimatum.server
  (:require [noir.server :as server]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.config :as config]))

(server/load-views-ns 'board-ultimatum.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))
        connection-info (if (nil? (:db-name config/storage))
                          (assoc config/storage :db-name "board_ultimatum")
                          config/storage)]
    (try
      (model/connect connection-info)
      (server/start port {:mode mode
                          :ns 'board-ultimatum})
      (catch java.io.IOException e
        (println "ERROR: Could not connect to MongoDB."))
      (catch java.lang.NullPointerException e
        (println "ERROR: Could not authenticate with Mongo. See config: \n\t"
                 (str (assoc connection-info :password "********")))))))
