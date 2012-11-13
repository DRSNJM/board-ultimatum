(ns board-ultimatum.engine.tag
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:use [monger.operators]
        [clojure.pprint]))


; These suck. Pulls all the data from mongo, and then filters. For
                                        ; each. Every time. Super dumb.
(defn all-raw-tags []
  (flatten
   (map (fn [g] (:tags g))
        (mc/find-maps "board_games"))))

(defn subtype? [subtype tag]
  (= (:subtype tag) subtype))

(defn raw-tags-by-subtype [subtype]
  (filter
   (partial subtype? subtype)
   (all-raw-tags)))

(defn raw-freq-tags-by-subtype [subtype]
  (sort-by (fn [tag] (* -1 (:frequency tag)))
           (map (fn [[tag freq]] (assoc tag :frequency freq))
                (frequencies (raw-tags-by-subtype subtype)))))

(defn uniq-tags-by-subtype [subtype]
  (distinct
   (raw-tags-by-subtype subtype)))

(defn create-tags-db-by-subtype [subtype]
  (mc/insert-batch "tags"
                   (map #(assoc % :pos-influence 50 :neg-influence -100 :group "Uncategorized")
                        (raw-freq-tags-by-subtype subtype))))

(defn reset-tags-table! []
  (do
    (mc/drop "tags")
    (mc/create "tags" {})
    (create-tags-db-by-subtype "mechanic")
    (create-tags-db-by-subtype "category")
    (create-tags-db-by-subtype "designer")
    (create-tags-db-by-subtype "publisher")))

(defn singular-subtype [attr]
  (cond
   (= attr "mechanics") "mechanic"
   (= attr "categories") "category"
   (= attr "designers") "designer"
   (= attr "publishers") "publisher"
   :else attr))

(defn tags-by-subtype [subtype]
  (sort-by #(* -1 (:frequency %)) (mc/find-maps "tags" {:subtype subtype})))

(defn mechanics []
  (tags-by-subtype "mechanic"))

(defn categories []
  (tags-by-subtype "category"))

(defn designers []
    (tags-by-subtype "designer"))

(defn publishers []
  (tags-by-subtype "publisher"))

(defn all-tags []
  (mc/find-maps "tags" {}))

(defn most-popular-categories []
  (map :value (raw-freq-tags-by-subtype "category")))

(defn most-popular-mechanics []
  (map :value (raw-freq-tags-by-subtype "mechanic")))

(defn update [subtype bgg-id new-data]
  (mc/update "tags" {:subtype subtype :bgg_id bgg-id} {$set new-data}))
