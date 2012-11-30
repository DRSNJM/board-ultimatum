(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.engine.tag :as tag]
            [board-ultimatum.views.attr-display :as attr-display]
            [board-ultimatum.views.results :as results]
            [clojure.string :as string])
  (:use [noir.core :only [defpage defpartial]]
        [clojure.pprint]))

;; Page for querying the logic based recommendation engine.
(defpage "/recommend" []
    (common/with-javascripts (cons "/js/recommend.js" common/*javascripts*)
      (common/layout

       [:div.row-fluid
        [:h1.row9.offset2 "Want a game recommendation?"]]

       [:div.row-fluid
        [:h4.row9.offset2 "You may select multiple options from each category."]]
       
        [:div#recommend.row-fluid
         [:div#sidebar.span2
          [:ul#select.nav.nav-pills.nav-stacked.affix
            [:li#length [:a "Game Length"]]
            [:li#num-players [:a "Number of Players"]]
            [:li#mechanics [:a "Mechanics"]]
            [:li#categories [:a "Categories"]]
            [:li#weight [:a "Weight"]]]]

         [:div.span9
          [:form#game-params {:action "/recommend" :method "post"}

            [:div {:id "input-length" :class "param well well-small"}
              [:input {:type "hidden" :name "length-active" :value "false"}]
              [:h3 "Game Length"]
              [:p "Approximate Game Length."]
              (map attr-display/time-checkboxes [20 30 45 60 90 120 180 240 300])]

            [:div {:id "input-num-players" :class "param well well-small"}
              [:input {:type "hidden" :name "num-players-active" :value "false"}]
              [:h3 "Number of Players"]
              (map attr-display/player-checkboxes ["1" "2" "3" "4" "5" "6" "7+"])]

            [:div {:id "input-mechanics" :class "param well well-small"}
              [:input {:type "hidden" :name "mechanics-active" :value "false"}]
              [:h3 "Mechanics"]
              [:p "Select gameplay mechanics that you like or dislike."]
              [:p "You may click on the center button of unselected mechanics for a brief description."]
              (map #(attr-display/build-tri-state % "mechanics")
                   (attr-display/columns 3 (tag/mechanics)))]

            [:div {:id "input-categories" :class "param well well-small"}
              [:input {:type "hidden" :name "categories-active" :value "false"}]
              [:h3 "Categories"]
              [:p "Select gameplay categories that you like or dislike."]
              [:p "You may click on the center button of unselected category for a brief description."]
              (map #(attr-display/build-tri-state % "categories")
                   (attr-display/columns 3 (tag/categories)))]
           
            [:div {:id "input-weight" :class "param well well-small"}
              [:input {:type "hidden" :name "weight-active" :value "false"}]
              [:h3 "Weight"]
              [:p "Weight describes how hard you have to think during the game. Heavier games require more analysis in game."]
              [:div {:class "btn-group" :data-toggle "buttons-radio"}
              (attr-display/build-radio-buttons 
                (array-map :Light "1" :Medium-Light "2"
                           :Medium "3" :Medium-Heavy "4"
                           :Heavy "5") 
                "weight")]]    
            [:button {:type "submit" :class "btn btn-submit"} "Submit"]]]])))

;; Should probaby do this filtering in js
(defn sanitize-query-params [attrs]
  (->> {}
       ((fn [hsh]
          (if (Boolean/valueOf (:mechanics-active attrs))
            (assoc hsh :mechanics
                   (map #(vector (name (first %)) (Integer/parseInt (second %)))
                        (filter #(not= "0" (second %)) (:mechanics attrs))))
            hsh)))
       ((fn [hsh]
          (if (Boolean/valueOf (:categories-active attrs))
            (assoc hsh :categories
                   (map #(vector (name (first %)) (Integer/parseInt (second %)))
                        (filter #(not= "0" (second %)) (:categories attrs))))
            hsh)))
       ((fn [hsh]
           (if (Boolean/valueOf (:num-players-active attrs))
             (assoc hsh :num-players
                    (flatten
                     (map #(if (= % "7+")
                             (range 7 100)
                             (Integer/parseInt %))
                          (:num-players attrs))))
           hsh)))
       ((fn [hsh]
           (if (Boolean/valueOf (:length-active attrs))
             (assoc hsh :length
                    (map #(Integer/parseInt %) (:length attrs)))
           hsh)))
       ((fn [hsh]
           (if (Boolean/valueOf (:weight-active attrs))
             (assoc hsh :weight (:weight attrs))
             hsh)))))

;; messy, but just debug info, so who cares?
(defpartial display-query-params [[attr-type values]]
  [:li attr-type ": "
   (if (vector? (first values))
     [:ul
      (map (fn [[k v]]
             (if (= 1 v)
               [:li.positive k]
               [:li.negative k]))
           values)]
     (string/join ", " values))])

(defpage [:post "/recommend"] {:as params}
  (common/with-javascripts
    (concat common/*javascripts* ["/js/bootstrap.js" "/js/results.js" "/js/spin.js"])
    (common/layout
     [:h1 "Have fun playing!"]
     [:h3 "Query Params"]
     [:div.well {:style "overflow:hidden;"}
      [:ul.query-params
       (map display-query-params (sanitize-query-params params))]]
     (results/build-results-list
      (take 60 (model/find-games
                (sanitize-query-params params)))
      true
      true))))
