(defproject board-ultimatum "0.1.0-SNAPSHOT"
  :description "Noir front-end to the board-ultimatum board game recomendation
               engine."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.3.0-beta10"]
                 [enclog "0.5.8-SNAPSHOT"]
                 [org.clojure/core.logic "0.7.5"]
                 [com.novemberain/monger "1.1.2"]]
  :main board-ultimatum.server)
