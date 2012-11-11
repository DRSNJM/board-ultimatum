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

(defpartial build-results-list [games disp-recom disp-explanation]

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