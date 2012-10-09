(ns board-ultimatum.views.expert
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/expert" []
  (common/layout
     [:h1 "Welcome, Board Game Expert!"]
     [:h2 "Please log in below"]))

(defn gameThumb [gId gName gDesc]
  [:div {:class "span3 well well-small" :id gId} 
    [:h4 gName] 
    [:p gDesc]
    [:div {:class "btn-group"}
      [:a {:class "btn btn-primary"}
        [:i {:class "icon-ok icon-white"}]]
      [:a {:class "btn"}
        [:i {:class "icon-remove"}]]]])

(defpage "/expert-select" []
  (common/layout
    [:h1 "Welcome, Board Game Expert!"]
    [:h2 "Please select games that you are familiar with"]
    [:div {:class "row-fluid"}

      (for [i (range 1 5)] 
        (gameThumb (str i) (str "Game" i) "Game picture and/or desc. goes here"))

    ]

    [:div {:class "row-fluid"} 

      (for [i (range 5 9)] 
        (gameThumb (str i) (str "Game" i) "Game picture and/or desc. goes here"))

    ])) 
