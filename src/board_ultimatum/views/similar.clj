(ns board-ultimatum.views.similar
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.model :as model]
            [board-ultimatum.views.results :as results]
            [board-ultimatum.flash :as flash]
            [clojure.string :as string]
            [clojure.data.json :as json])
  (:use [noir.core :only [defpage defpartial]]
        [noir.response :only [redirect]]
        [hiccup.element]
        [hiccup.form]
        [clojure.pprint]))

(defn names-to-json []
  (json/write-str (map (fn [game] (:name game)) (model/find-all))))

;; Page for searching for "similar" games
(defpage "/similar" []
    (common/with-javascripts (cons "/js/similar.js" common/*javascripts*)
      (common/layout
        [:script {:type "text/javascript"} (str "var taValues = " (names-to-json) ";")]
        [:h1 "Liked a game?"]
        [:h2 "Enter the name below to find more that you will enjoy!"]
        [:div#recommend.row-fluid
          [:form#game-params {:action "/similar" :method "post"}
            [:div.input-append
              [:input#game-name {:type "text" :data-provide "typeahead" :name "game-name"}]
              [:button {:type "submit" :class "btn"} "Search"]]]])))

(defpage [:post "/similar"] {:as params}
    (if
      (nil? (model/get-id-by-name (:game-name params)))
        (do (flash/put! :error (str (:game-name params) " not found in database")) 
            (redirect "/similar"))
      (common/with-javascripts (cons "/js/similar.js" common/*javascripts*)
        (common/layout      
          [:h1 "Game Results"]
          [:h2 "Based on \"" (:game-name params) "\""]
          (let [game-ids
            (sort-by :rating >
              (model/get-similar 
                (model/get-id-by-name 
                  (:game-name params))))]
            (results/build-results-list
              (map #(model/get-game-by-id (:game_b %)) (take 30 game-ids))
              false
              false
              (map :rating game-ids)))
          [:h4 [:a {:href "/similar"} "Search again"]]))))


