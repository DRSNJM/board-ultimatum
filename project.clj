(defproject board-ultimatum "0.1.0-SNAPSHOT"
  :description "Noir front-end to the board-ultimatum board game recomendation
               engine."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.3.0-beta10"]
                 [board-ultimatum-engine "0.1.0-SNAPSHOT"]
                 [org.clojure/core.logic "0.7.5"]
                 [com.leadtune/clj-ml "0.2.4"]]
  :main board-ultimatum.server)