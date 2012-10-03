(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/recommend" []
    (common/layout
        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
        [:form {:action "/recommend" :method "post"} 
            [:dl [:dt "Number of players"]
                 [:dd [:input {:name "num-players"}]]]
            [:input {:type "submit" :value "Search"}]]))

(defpage [:post "/recommend"] {:keys [num-players]}
    (common/layout
        [:h1 "Here are your results"]
        [:h2 "Have fun playing!"]
        [:p (str "You wanted a game with " num-players " players")]))