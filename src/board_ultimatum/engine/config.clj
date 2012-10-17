(ns board-ultimatum.engine.config
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn load-map-from-resource
  "Load the given resource path as a clojure file and return its content."
  [resource-path]
  (if-let [cres (io/resource resource-path)]
    (-> cres (.getPath) (load-file))
    {}))

;; Define a map read from config.clj on the resource path.
(def ^:dynamic config-from-file (load-map-from-resource "config.clj"))

(defn read-config
  "Reads config by first accessing the map from the config file and falling back
  on environment variables. This is heroku \"friendly\"."
  [config-var]
  (get config-from-file
       (keyword (string/replace (string/lower-case config-var) "_" "-"))
       (System/getenv config-var)))

;; These values are meant for setting up mongo.
(def storage {:uri (read-config "MONGOHQ_URL")
              :db-name (read-config "MONGO_DB_NAME")
              :username (read-config "MONGO_USERNAME")
              :password (read-config "MONGO_PASSWORD")})
