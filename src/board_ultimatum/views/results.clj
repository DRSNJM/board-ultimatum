(ns board-ultimatum.views.results
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.attr-display :as attr-display]
            [clojure.string :as string])
  (:use [hiccup.element]
        [noir.core]))

(defpartial pp-factor [factor]
  [:div {:style "clear:left;"}
    [:div {:style "float:left;"} (:reason factor)]
    [:div {:style "float:right;"} (attr-display/format-score (:score factor))]])

(defpartial pp-factors [game]
  (map pp-factor (:factors game))
  [:div {:style "clear:left;border-top: 1px solid #666666;padding: 5px 0px;"}
    [:b
      [:div {:style "float:left;"} "Total score"]
      [:div {:style "float:right;"} (attr-display/format-score (:score game))]]])

(defpartial display-game [i game disp-recom disp-explanation]
  [:div.well.game {:style "height:150px;position:relative;"}
    [:div.pop-trigger {:style "position:absolute;left:-5px;top:2px;float:left;"
         :rel "popover" :data-placement "left" :data-trigger "hover"
         :data-title "How did this game match up to your preferences?"}
      [:i {:class "icon-question-sign" :style "margin-left:10px;"}]]
    [:div.pop-content {:style "display:none;"} (pp-factors game)]
    [:div {:style "width:200px;float:left;"}
      [:img {:src (:thumbnail game) :style "margin: 0px auto;display: block;"}]]
    [:table {:style "float:left;margin-left:20px;width:75%;line-height:normal;"}
      [:tr {:style "height:50px;border-bottom:1px solid black;"}
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
            [:div {:style "text-align:center;border-left:1px solid black;"}
              [:div {:style "font-size:30px;"} (first length-disp)]
              [:div (second length-disp)]])]
        [:td {:style "width:15%;"}
          (let [num-pl-disp
            (string/split 
              (attr-display/num-players (:min_players game) (:max_players game))
              #"\s+")]
            [:div {:style "text-align:center;border-left:1px solid black;"}
              [:div {:style "font-size:30px;"} (first num-pl-disp)]
              [:div (second num-pl-disp)]])
          ]
        [:td {:style "width:15%;"}
          [:div {:style "text-align:center;border-left:1px solid black;"}
            [:div {:style "font-size:30px;"} (:min_age game) "+"]
            [:div "years old"]]]]]])


(defpartial build-results-list [games disp-recom disp-explanation]
    (map display-game
      (iterate inc 1)
      games
      (iterate identity disp-recom)
      (iterate identity disp-explanation)))