(ns board-ultimatum.views.results
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.recommend :as recommend])
  (:use [hiccup.element]))

(defn pp-factors [game]
  (interpose "<br/>"
    (map 
      #(str (:reason %) ": "
        (format-score (:score %)))
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
   [:td (recommend/game-length (:length game))]
   [:td (recommend/num-players (:min_players game) (:max_players game))]
   [:td (:min_age game) "+"]
   [:td (recommend/format-score (:score game)) " points"]
   [:td.why (pp-factors game)]])

(defn build-results-list [games disp-recom disp-explanation]

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