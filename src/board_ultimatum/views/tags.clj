(ns board-ultimatum.views.tags
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.engine.tag :as tag])
  (:use [noir.core]
        [hiccup.core]
        [hiccup.element]
        [hiccup.form]
        [clojure.pprint]
        [monger.result]))

(defpartial tag-item [{:keys [value bgg_id description group subtype pos-influence neg-influence frequency]}]
    (with-group subtype
      (with-group bgg_id
        [:tr
         [:td value]
         [:td (text-field :description description)]
         [:td (text-field :group group)]
         [:td (text-field :pos-influence pos-influence)]
         [:td (text-field :neg-influence neg-influence)]
         [:td frequency]])))

(defpartial subtype-table [subtype]
  [:h3 (clojure.string/capitalize subtype)]
  [:table.table.table-striped.table-condensed
   [:thead
    [:th "Name"]
    [:th "Description"]
    [:th "Group"]
    [:th "Positive Influence"]
    [:th "Negative Influence"]
    [:th "Frequency"]]
   [:tbody
    (map tag-item (tag/tags-by-subtype subtype))]])

(defpage "/tags" []
  (common/layout
   [:h1 "Tag Administration"]
   (form-to [:post "/tags"]
            (map subtype-table ["mechanic" "category"])
            (submit-button "Save Corrections"))))

(defn update-tag [subtype params]
  (doall (map (fn [[bgg-id data]]
                (tag/update subtype (Integer/parseInt bgg-id) data))
              (subtype params)))  )

(defpage [:post "/tags"] [:as params]
  (do
    (update-tag :mechanic params)
    (update-tag :category params)
    (render "/tags")))
