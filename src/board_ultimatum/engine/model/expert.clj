(ns board-ultimatum.engine.model.expert
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [board-ultimatum.views.common :as common]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use [monger.operators]))

(def ^:private coll
  "The name of the collection on mongo containing experts."
  "experts")

(defn exists?
  "An expert with the given id exists in the database."
  [id] (boolean (mc/find-one coll {:identifier id} [:identifier])))

(defn from-id
  "Get an expert from the database by id."
  [id]
  (mc/find-one-as-map coll {:identifier id}))

(defn add
  "Add an expert with the given id to the datastore."
  [id]
  (mc/insert coll {:identifier id
                   :sessions-count 0
                   :relations-count 0
                   :not-seen []
                   :seen []
                   :relations []}))
