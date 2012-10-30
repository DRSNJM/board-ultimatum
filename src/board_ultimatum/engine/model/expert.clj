(ns board-ultimatum.engine.model.expert
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [board-ultimatum.views.common :as common]
            [clojure.string :as string]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use [monger.operators]))

(def ^:private coll
  "The name of the collection on mongo containing experts."
  "experts")

(defn exists?
  "An expert with the given id exists in the database."
  [id] (boolean (mc/find-one coll
                             {:identifier (string/lower-case id)}
                             [:identifier])))

(defn from-id
  "Get an expert from the database by id."
  ([id fields] (mc/find-one-as-map coll {:identifier (string/lower-case id)} fields))
  ([id] (from-id id [])))

(defn name-from-id [id]
  "Get the name of the expert with the given id in its pretty form if it
  exists."
  (let [expert (from-id id [:identifier :pretty-id])]
       (get expert :pretty-id
            (get expert :identifier))))

(defn add
  "Add an expert with the given id to the datastore."
  [id]
  (mc/insert coll {:pretty-id id
                   :identifier (string/lower-case id)
                   :sessions-count 0
                   :relations-count 0
                   :not-seen []
                   :seen []
                   :relations []}))
