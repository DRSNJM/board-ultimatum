(ns board-ultimatum.views.results
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.attr-display :as attr-display]
            [clojure.string :as string])
  (:use [hiccup.element]
        [noir.core]))

(defn pp-factors [game]
  (interpose "<br/>"
    (map 
      #(str (:reason %) ": "
        (attr-display/format-score (:score %)))
      (:factors game))))

(defn display-game [i game]
  [:tr.game
   [:td (+ 1 i) "."]
   [:td (image (:thumbnail game))]
   [:td (link-to (str "http://boardgamegeek.com/boardgame/" (:bgg_id game) "/")
    (:rank game) ". ")]
   [:td.name
    [:div.game-name (:name game)]
    [:ul
     (map (fn [e] [:li e])
       (concat (model/mechanics game)
         (model/categories game)))]]
   [:td (attr-display/game-length (:length game))]
   [:td (attr-display/num-players (:min_players game) (:max_players game))]
   [:td (:min_age game) "+"]
   [:td (attr-display/format-score (:score game)) " points"]
   [:td.why (pp-factors game)]])

(defpartial display-game2 [i game disp-recom disp-explanation]
  [:div.well {:style "height:150px;"}
    [:i {:class "icon-question-sign"
         :style "position:relative;right:15px;bottom:15px;float:left;"
         :rel "popover" :data-placement "left" :data-trigger "hover"
         :data-title "How did this game match up to your preferences?"}]
    [:div.pop-content {:style "display:none;"} (pp-factors game)]
    [:div {:style "width:200px;float:left;"}
      [:img {:src (:thumbnail game) :style "margin: 0px auto;display: block;"}]]
    [:table {:border "1" :style "float:left;margin-left:20px;width:75%;line-height:normal;"}
      [:tr {:style "height:50px;"}
        [:td {:colspan "4"} 
          [:div {:style "font-size:34px;float:left;"}
            (:name game)]
          [:div {:style "float:right;"} 
            (link-to 
              (str "http://boardgamegeek.com/boardgame/" (:bgg_id game) "/")
              "BGG Rank: " (:rank game) " "
              [:i {:class "icon-share"}])]]]
      [:tr {:style "height:80px;"}
        [:td {:style "width:55%;"}
          (map #(identity [:span {:style "width:50%;float:left;margin-bottom:2px;"} "&#149; " %])
            (concat (model/mechanics game)
              (model/categories game)))]
        [:td {:style "width:15%;"}
          (let [length-disp
            (string/split
              (attr-display/game-length (:length game))
              #"\s+")]
            [:div {:style "text-align:center;"}
              [:div {:style "font-size:30px;"} (first length-disp)]
              [:div (second length-disp)]])]
        [:td {:style "width:15%;"}
          (let [num-pl-disp
            (string/split 
              (attr-display/num-players (:min_players game) (:max_players game))
              #"\s+")]
            [:div {:style "text-align:center;"}
              [:div {:style "font-size:30px;"} (first num-pl-disp)]
              [:div (second num-pl-disp)]])
          ]
        [:td {:style "width:15%;text-align:center;"}
          [:div {:style "font-size:30px;"} (:min_age game) "+"]
          [:div "years old"]]]]])

(defpartial build-results-list [games disp-recom disp-explanation]

    (map display-game2
      (iterate inc 1)
      games
      (iterate identity disp-recom)
      (iterate identity disp-explanation))

    [:table.games.table.table-striped
    [:thead
     [:th "#"]
     [:th "Thumb"]
     [:th "BGG Rank"]
     [:th "Name"]
     [:th "Length"]
     [:th "Num Players"]
     [:th "Min Age"]
     [:th "Score"]
     [:th "Why?"]]
    [:tbody 
     (map-indexed display-game games)]])