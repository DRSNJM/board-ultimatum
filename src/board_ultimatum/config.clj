(ns board-ultimatum.config
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

;; Define a map read from config.clj on the resource path.
(def config-from-file
  (if-let [cres (io/resource "config.clj")]
    (-> cres (.getPath) (load-file))
    {}))

(defn read-config
  "Reads config by first accessing the map from the config file and falling back
  on environment variables. This is heroku \"friendly\"."
  [config-var]
  (get config-from-file
       (keyword (string/replace (string/lower-case config-var) "_" "-"))
       (System/getenv config-var)))
