(ns board-ultimatum.views.expert
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

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

(defn game-thumb [game-id game-name imgURL]
  [:div {:class "span3 well well-small" :id game-id}
  ;[:div {:class (str "span" (/ 12 grid-cols))}
  [:button {:type "button" :class "btn btn-info btn-input" :data-toggle "button"}
    [:input {:type "hidden" :name game-id :value "off"}]
    [:img {:src imgURL :class "img-rounded"}]
    [:h4 game-name]]])

(defpage [:get "/expert-select"] []
  (common/layout
    [:script {:type "text/javascript"}
      "// make game selections toggle hidden input
      $('.btn-input').live('click', function(event){
        event.preventDefault();
        var input = $(this).find('input[type=hidden]');
        if (input.val() == 'off') {
          input.val('on');
        } else {
          input.val('off');
        }
      });"]

    [:h1 "Welcome, Board Game Expert!"]
    [:h2 "Please select games that you are familiar with:"]

    [:form {:method "post"}
      [:div {:class "container"}
        (let [build-game-thumb #(game-thumb (first %) (second %) (last %))]
        (map #(map build-game-thumb %) retrieved-games))

        [:div {:class "row"}
          [:button {:type "submit" :class "btn btn-large btn-primary span3 offset2"} "I know these games!"]
          [:a {:href "/expert-select" :class "btn btn-large span3"} "Try the next set"]]]]))

(defpage [:post "/expert-select"] {:as m} 
  (str "parameters: " m))