(ns board-ultimatum.views.similar
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.model :as model]
            [clojure.string :as string]
            [clojure.data.json :as json])
  (:use [noir.core :only [defpage defpartial]]
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
              [:input#game-name {:type "text" :data-provide "typeahead"}]
              [:button {:type "submit" :class "btn"} "Search"]]]])))
           

