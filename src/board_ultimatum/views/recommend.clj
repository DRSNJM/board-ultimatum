(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common])
  (:require [board-ultimatum.engine.model :as model])
  (:use [noir.core :only [defpage]]
        [hiccup.element]))

(defpage "/recommend" []
    (common/layout

        [:script {:type "text/javascript"}
          "$(document).ready(function(){
            $('.param').hide();
            
            $('#select li').click(function() {
              $(this).toggleClass('active');
              $('#input-' + $(this).attr('id')).toggle('medium', function() {
              });
              var active = $('input[name=' + $(this).attr('id') + '-active]');
              $(active[0]).attr('value', $(active[0]).attr('value') == 'false' ? 'true' : 'false');
              
            });
          });"
        ]

        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
        [:div {:class "row-fluid"}

          [:ul {:id "select" :class "span3 nav nav-pills nav-stacked"}
            [:li {:id "param1" } [:a {:href "#"} "param1"]]
            [:li {:id "param2" } [:a {:href "#"} "param2"]]
            [:li {:id "param3" } [:a {:href "#"} "param3"]]
            [:li {:id "param4" } [:a {:href "#"} "param4"]]]

          [:form {:id "game-params" :class "span9" :action "/recommend" :method "post"} 

            [:div {:id "input-param1" :class "param well well-small"}
              [:h3 "param1"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param1-active" :value "false"}]
              [:input {:type "text" :name "param1-value"}]]

            [:div {:id "input-param2" :class "param well well-small"}
              [:h3 "param2"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param2-active" :value "false"}]
              [:input {:type "text" :name "param2-value"}]]

            [:div {:id "input-param3" :class "param well well-small"}
              [:h3 "param3"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param3-active" :value "false"}]
              [:input {:type "text" :name "param3-value"}]]

            [:div {:id "input-param4" :class "param well well-small"}
              [:h3 "param4"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param4-active" :value "false"}]
              [:input {:type "text" :name "param4-value"}]]

              
            [:button {:type "submit" :class "btn"} "Submit"]]
        ]))

(defn num-players [min-players max-players]
  (cond
   (= min-players max-players) (str max-players " Player")
   :else (str min-players "-" max-players " Players")))

(defn game-length [length]
  (cond
   (>= length 120) (str (/ length 60) " hours")
   :else (str length " minutes")))

(defn display-game [game]
  [:tr.game
   [:td (:rank game) ". "]
   [:td (image (:thumbnail game))]
   [:td (:bgg_id game) ". "]
   [:td (:name game)]
   [:td (game-length (:length game))]
   [:td (num-players (:min_players game) (:max_players game))]
   [:td (:min_age game) "+"]])

(defpage [:post "/recommend"] {:keys [lengths]}
  (println "POST PARAMS: " lengths)
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
               (sort #(compare (:rank %1) (:rank %2))
                     (model/find-by-length lengths)))]]))

; (model/find-by-players 5 10)
