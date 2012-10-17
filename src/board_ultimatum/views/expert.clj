(ns board-ultimatum.views.expert
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage defpartial]]))

; grid-cols must divide 12
(def grid-cols 3)
(def grid-rows 3)
(def grid-size (* grid-cols grid-rows))

; Construct list of game titles and cover images
(def retrieved-games
  ; Currently produces dummy values, will eventually retrieve random games from database
  (for [x (range grid-cols)]
    (for [y (range grid-rows)]
      [(+ (* x grid-cols) y) "Game Title: Might Be a Bit Long" "http://lorempixel.com/150/100/"])))


(defpartial game-thumb [[game-id game-name imgURL]]
  [:div.span4.game {:id (str "game-" game-id)}
   [:button.btn.btn-info.btn-input {:data-toggle "button"}
    [:input {:type "hidden" :name (str "game-field-" game-id) :value "off"}]
    [:img.img-rounded {:src imgURL}]
    [:h4 game-name]]])

(defpartial grid-row [coll]
  [:div.row-fluid
   (map game-thumb coll)])

(defpage "/expert-select" []
  (common/with-javascripts (cons "/js/expert.js" common/*javascripts*)
    (common/layout
      [:h1 "Welcome, Board Game Expert!"]
      [:h2 "Please select games that you are familiar with:"]
      [:form#expert-select.container-fluid {:method "post"}
       (map grid-row retrieved-games)
       [:div.row-fluid
        [:button.btn.btn-large.btn-primary.span6 "I know these games!"]
        [:a.btn.btn-large.span6 {:href "/expert-select"} "Try the next set"]]])))

(defpage [:post "/expert-select"] {:as m}
  (str "parameters: " m))
