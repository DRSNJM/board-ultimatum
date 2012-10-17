(ns board-ultimatum.views.recommend-alt
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]
        [hiccup.element]
        [hiccup.form]))

(defn player-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div.icon.player]
    (check-box num false num)
    [:span.bottom-label (str num " Players")]]])

(defn time-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div.icon.time]
    (check-box num false num)
    [:span.bottom-label num]]])

(defpage "/recommend_alt" []
  (common/layout
   (form-to {:class "form-inline"} [:put "/post"]
            [:div {:class "players"}
             [:h1 "Players"]
             (map player-checkboxes ["1" "2" "3" "4" "5" "6" "7+"])]

            [:div {:class "length"}
             [:h1 "Game Length"]
             (map time-checkboxes ["<20 minutes" "~30 minutes" "~45 minutes" "~1 hour" "~2 hours" "~3 hours" "~4 hours" "5+ hours"])]

            [:div {:class "tags"}
             [:h1 "Mechanics / Categories"]]

            )))
