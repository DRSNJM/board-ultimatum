(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common])
  (:require [board-ultimatum.engine :as engine])
  (:use [noir.core :only [defpage defpartial]])
  (:use [hiccup.page]))

;; Build preference selection button
(defpartial build-tri-state [name form-name]
  [:div
    [:div {:class "btn-group tri-state"}
      [:button {:type "button" :class "btn btn-danger"} [:i {:class "icon-thumbs-down"}]]
      [:button {:type "button" :class "btn option"} name]
      [:button {:type "button" :class "btn btn-success"} [:i {:class "icon-thumbs-up"}]]]
   [:input {:type "hidden" :name (str "mechanic[" form-name "]") :value "0"}]])

(defpage "/recommend" []
    (common/layout
      (include-js "/js/recommend.js")

        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
        [:div {:class "row-fluid"}

          [:ul {:id "select" :class "span2 offset2 nav nav-pills nav-stacked affix"}
            [:li {:id "length" :style "cursor:pointer;"} [:a "Game Length"]]
            [:li {:id "num-players" :style "cursor:pointer;"} [:a "Number of Players"]]
            [:li {:id "mechanics" :style "cursor:pointer;"} [:a "Mechanics"]]
            [:li {:id "weight" :style "cursor:pointer;"} [:a "Weight"]]]

          [:form {:id "game-params" :class "span4 offset5" :action "/recommend" :method "post"}

            [:div {:id "input-length" :class "param well well-small"}
              [:input {:type "hidden" :name "length-active" :value "false"}]
              [:h3 "Game Length"]
              [:p "This is a description of this field"]
              [:input {:type "text" :name "length-value"}]]

            [:div {:id "input-num-players" :class "param well well-small"}
              [:input {:type "hidden" :name "num-players-active" :value "false"}]
              [:h3 "Number of Players"]
              [:p "This is a description of this field"]
              [:input {:type "text" :name "num-players-value"}]]

            [:div {:id "input-mechanics" :class "param well well-small"}
              [:input {:type "hidden" :name "mechanics-active" :value "false"}]
              [:h3 "Mechanics"]
              [:p "Select gameplay mechanics that you like or dislike"]
              (build-tri-state "Hand Management" "card-draft")
              (build-tri-state "Deck Building" "card-draft")
              (build-tri-state "Card Drafting" "card-draft")
              ]

            [:div {:id "input-weight" :class "param well well-small"}
              [:input {:type "hidden" :name "weight-active" :value "false"}]
              [:h3 "Weight"]
              [:p "This is a description of this field"]
              [:input {:type "text" :name "weight-value"}]]

              
            [:button {:type "submit" :class "btn"} "Submit"]]]))


;; Process queries received from the interface
(defpage [:post "/recommend"] {:as params}
    (common/layout
        [:h1 "Here are your results"]
        [:h2 "Have fun playing!"]
        [:p (str "Your games: " (str params))]))
