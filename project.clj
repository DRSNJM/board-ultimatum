(defproject board-ultimatum "0.1.0-SNAPSHOT"
  :description "Noir front-end to the board-ultimatum board game recomendation
               engine."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.3.0-beta10"]
                 [enclog "0.5.8-SNAPSHOT"]
                 [org.clojure/core.logic "0.7.5"]
                 [com.novemberain/monger "1.1.2"]
                 [org.clojure/math.combinatorics "0.0.3"]
                 [incanter "1.2.4"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                   :plugins [[lein-exec "0.2.1"]
                             [lein-kibit "0.0.7"]
                             [lein-midje/lein-midje "2.0.1"]]}}
  :main board-ultimatum.server)
