(ns board-ultimatum.views.results
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.attr-display :as attr-display])
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
    [:div {:style "width:200px;float:left;"}
      [:img {:src (:thumbnail game) :style "margin: 0px auto;display: block;"}]]
    [:table {:border "1" :style "float:left;margin-left:20px;width:75%;"}
      [:tr {:style "line-height:50px;"}
        [:td {:colspan "4" :style "font-size:34px;"}
          (:name game)]]
      [:tr {:style "height:80px;line-height:50px;"}
        [:td {:style "width:55%;"}
          "HEY"]
        [:td {:style "width:15%;"}
          (attr-display/game-length (:length game))]
        [:td {:style "width:15%;"}
          (attr-display/num-players (:min_players game) (:max_players game))]
        [:td {:style "width:15%;"}
          (:min_age game) "+"]]]])

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