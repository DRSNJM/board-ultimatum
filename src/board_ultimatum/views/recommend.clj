(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/recommend" []
         (common/layout
           [:h1 "Want a game recommendation?"]
           [:h2 "Fill in the inputs below with your preferences"]
           [:form {:action "/games"} [:dl [:dt "Number of players"]
                       [:dd [:input {:id "num-players"}]]]
                  [:input {:type "submit" :value "Search"}]]))
