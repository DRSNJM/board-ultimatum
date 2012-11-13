(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.attr-display :as attr-display]
            [board-ultimatum.views.results :as results]
            [clojure.string :as string])
  (:use [noir.core :only [defpage defpartial]]
        [clojure.pprint]))

;; Page for querying the logic based recommendation engine.
(defpage "/recommend" []
    (common/with-javascripts (cons "/js/recommend.js" common/*javascripts*)
      (common/layout
        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
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
              [:p "This is a description of this field"]
              (map attr-display/time-checkboxes [20 30 45 60 90 120 180 240 300])]

            [:div {:id "input-num-players" :class "param well well-small"}
              [:input {:type "hidden" :name "num-players-active" :value "false"}]
              [:h3 "Number of Players"]
              [:p "This is a description of this field"]
              (map attr-display/player-checkboxes ["1" "2" "3" "4" "5" "6" "7+"])]

            [:div {:id "input-mechanics" :class "param well well-small"}
              [:input {:type "hidden" :name "mechanics-active" :value "false"}]
              [:h3 "Mechanics"]
              [:p "Select gameplay mechanics that you like or dislike"]
              (map #(attr-display/build-tri-state % "mechanics" %)
                   model/most-popular-mechanics)]

            [:div {:id "input-categories" :class "param well well-small"}
              [:input {:type "hidden" :name "categories-active" :value "false"}]
              [:h3 "Categories"]
              [:p "Select gameplay categories that you like or dislike"]
              (map #(attr-display/build-tri-state % "categories" %)
                   model/most-popular-categories)]

           
            [:div {:id "input-weight" :class "param well well-small"}
              [:input {:type "hidden" :name "weight-active" :value "false"}]
              [:h3 "Weight"]
              [:p "This is a description of this field"]
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
                    (map #(Integer/parseInt %) (:num-players attrs)))
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
    (concat common/*javascripts* ["/js/bootstrap.js" "/js/results.js"])
    (common/layout
     [:h1 "Have fun playing!"]
     [:h3 "Query Params"]
     [:div.well {:style "overflow:hidden;"}
      [:ul.query-params (map
            display-query-params
            (sanitize-query-params params))]]
      (results/build-results-list
        (model/find-games
          (sanitize-query-params params))
        true
        true
        nil))))