(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.model :as model])
  (:use [noir.core :only [defpage defpartial]]
        [hiccup.element]
        [hiccup.form]))

;; Build preference selection button
(defpartial build-tri-state [name form-name]
  [:div
    [:div {:class "btn-group tri-state"}
      [:button {:type "button" :class "btn btn-danger"} [:i {:class "icon-thumbs-down"}]]
      [:button {:type "button" :class "btn option"} name]
      [:button {:type "button" :class "btn btn-success"} [:i {:class "icon-thumbs-up"}]]]
   [:input {:type "hidden" :name (str "mechanic[" form-name "]") :value "0"}]])

(defn game-length [length]
  (cond
   (>= length 120) (str (/ length 60) " hours")
   :else (str length " minutes")))

(defn player-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div.icon.player]
    (check-box num false num)
    [:div.bottom-label (str num " Players")]]])

(defn time-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div.icon.time]
    (check-box "length[]" false num)
    [:div.bottom-label (game-length num)]]])

;; Page for querying the logic based recommendation engine.
(defpage "/recommend" []
    (common/with-javascripts (cons "/js/recommend.js" common/*javascripts*)
      (common/layout
        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
        [:div#recommend.row-fluid
         [:div#sidebar.span3
          [:ul#select.span3.nav.nav-pills.nav-stacked.affix
            [:li#length [:a "Game Length"]]
            [:li#num-players [:a "Number of Players"]]
            [:li#mechanics [:a "Mechanics"]]
            [:li#weight [:a "Weight"]]]]

         [:div.span9
          [:form#game-params {:action "/recommend" :method "post"}

            [:div {:id "input-length" :class "param well well-small"}
              [:input {:type "hidden" :name "length-active" :value "false"}]
              [:h3 "Game Length"]
              [:p "This is a description of this field"]
              (map time-checkboxes [20 30 45 60 90 120 180 240 300])]

            [:div {:id "input-num-players" :class "param well well-small"}
              [:input {:type "hidden" :name "num-players-active" :value "false"}]
              [:h3 "Number of Players"]
              [:p "This is a description of this field"]
              (map player-checkboxes ["1" "2" "3" "4" "5" "6" "7+"])]

            [:div {:id "input-mechanics" :class "param well well-small"}
              [:input {:type "hidden" :name "mechanics-active" :value "false"}]
              [:h3 "Mechanics"]
              [:p "Select gameplay mechanics that you like or dislike"]
              (build-tri-state "Hand Management" "hand-management")
              (build-tri-state "Deck Building" "deck-building")
              (build-tri-state "Card Drafting" "card-draft")]

            [:div {:id "input-weight" :class "param well well-small"}
              [:input {:type "hidden" :name "weight-active" :value "false"}]
              [:h3 "Weight"]
              [:p "This is a description of this field"]
              [:input {:type "text" :name "weight-value"}]]
            [:button {:type "submit" :class "btn"} "Submit"]]]])))


(defn num-players [min-players max-players]
  (cond
   (= min-players max-players) (str max-players " Player")
   :else (str min-players "-" max-players " Players")))

(defn display-game [game]
  [:tr.game
   [:td (:rank game) ". "]
   [:td (image (:thumbnail game))]
   [:td (:bgg_id game) ". "]
   [:td (:name game)]
   [:td (game-length (:length game))]
   [:td (num-players (:min_players game) (:max_players game))]
   [:td (:min_age game) "+"]])

(defpage [:post "/recommend"] {:as params}
;  (println "POST PARAMS: " (params :length))
    (common/layout
      [:h1 "Have fun playing!"]
      [:table.games.table.table-striped
       [:thead
        [:th "Rank"]
        [:th "Thumb"]
        [:th "BGG ID"]
        [:th "Name"]
        [:th "Length"]
        [:th "Num Players"]
        [:th "Min Age"]]
       [:tbody
        (map display-game
          (take 30
            (sort-by :rank
              (model/find-games
                (map #(Integer/parseInt %)
                  (params :length))
                ))))]]))

