(ns board-ultimatum.views.results
  "Namespace of html generating code for displaying games on a results page.
  External users of this namespace should only need to call build-results-list."
  (:require [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.attr-display :as attr-display]
            [clojure.string :as string])
  (:use [hiccup.element]
        [noir.core]))

(defn game-weight-text [weight]
  (cond
    (< weight 1.5) "Light"
    (< weight 2.5) "Medium Light"
    (< weight 3.5) "Medium"
    (< weight 4.5) "Medium Heavy"
    :else "Heavy"))

(defpartial pp-factor [factor]
  [:div {:style "clear:both;"}
    [:div {:style "float:left;"} (:reason factor)]
    [:div {:style "float:right;"} (attr-display/format-score (:score factor))]])

(defpartial pp-factors [game]
  (map pp-factor (:factors game))
  [:div {:style "clear:left;border-top: 1px solid #666666;padding: 5px 0px;"}
    [:b
      [:div {:style "float:left;"} "Total score"]
      [:div {:style "float:right;"} (attr-display/format-score (:score game))]]])

(defpartial pp-factors-trigger [game]
  [:div
    [:div.pop-trigger {:style "position:absolute;right:5px;top:2px;float:left;display:none;"
         :rel "popover" :data-placement "left" :data-trigger "hover"
         :data-title "How did this game match up to your preferences?"}
      [:i.icon-question-sign {:style "margin-left:10px;"}]]
    [:div.pop-content {:style "display:none;"} (pp-factors game)]])

(defpartial mechanics-categories [game]
  (map #(identity [:span {:style "width:50%;float:left;margin-bottom:2px;"} "&#149; " %])
    (concat (model/mechanics game)
      (model/categories game))))

(defpartial length [game]
  (let [length-disp
    (string/split (attr-display/game-length (:length game)) #"\s+")]
    [:div {:style "text-align:center;border-left:1px solid black;"}
      [:div {:style "font-size:30px;"} (first length-disp)]
      [:div (second length-disp)]]))

(defpartial num-players [game]
  (let [num-pl-disp
    (string/split (attr-display/num-players (:min_players game) (:max_players game)) #"\s+")]
    [:div {:style "text-align:center;border-left:1px solid black;"}
      [:div {:style "font-size:30px;"} (first num-pl-disp)]
      [:div (second num-pl-disp)]]))

(defpartial min-age [game]
  [:div {:style "text-align:center;border-left:1px solid black;"}
    [:div {:style "font-size:30px;"} (:min_age game) "+"]
    [:div "years old"]])

(defpartial weight [game]
  [:div {:style "text-align:center;border-left:1px solid black;"}
    (let [text (string/split (game-weight-text (:weight_average game)) #"\s+")]
      (if
        (< 1 (.size text))
        [:b (map #(identity [:div {:style "font-size:16px;"} %]) text)]
        (identity [:div {:style "font-size:30px;"} (first text)])))
    [:div "weight"]])

(defpartial recom-trigger []
  [:div {:style "position:absolute;left:50%;bottom:0px;"}
    [:div.open-recom {:style "display:none;position:relative;left:-50%;height:30px;width:40px;"}
      [:i.icon-chevron-down]]])

(defpartial display-game [i game disp-recom disp-explanation rating]
  [:div.well.game {:style "height:150px;position:relative;"}
    (if (and disp-explanation (not= (.size (:factors game)) 0))
      (pp-factors-trigger game))
    (if disp-recom (recom-trigger))
    [:div {:style "width:20%;float:left;"}
      [:img.img-polaroid {:src (:thumbnail game) :style "margin: 0px auto;display: block;"}]]
    [:table {:style "float:right;margin-left:20px;width:78%;line-height:normal;"}
      [:tr {:style "height:50px;border-bottom:1px solid black;"}
        [:td {:colspan "5"}
          [:div {:style "font-size:34px;float:left;"}
            (:name game)
            (when-not (nil? rating) (str " - " (format "%.1f" (* 100 rating)) "% Match"))]
          [:div {:style "float:right;"} 
            (link-to
              (str "http://boardgamegeek.com/boardgame/" (:bgg_id game) "/")
              "BGG Rank: " (:rank game)
              [:i.icon-share {:style "margin-left:4px;"}])]]]
      [:tr {:style "height:80px;"}
        [:td {:style "width:50%;"}
          (mechanics-categories game)]
        [:td {:style "width:11%;"}
          (length game)]
        [:td {:style "width:11%;"}
          (num-players game)]
        [:td {:style "width:11%;"}
          (min-age game)]
        [:td {:style "width:17%;"}
          (weight game)]]]])

;; Send true for disp-recom, disp-explanation if you wish to display recommendations
;; and explanations on games. Ratings should be nil if no ratings are to be displayed.
(defpartial build-results-list [games disp-recom disp-explanation ratings]
    (map display-game
      (iterate inc 1)
      games
      (iterate identity disp-recom)
      (iterate identity disp-explanation)
      (if ratings
        ratings
        (cycle [nil]))))
