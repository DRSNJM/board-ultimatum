(ns board-ultimatum.engine.model.relationship
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:use [monger.operators]))

(def coll
  "The name of the collection on mongo containing relationships."
  "relationships")

(defn exists?
  "An relationship between the two games exists in the database."
  [games] (boolean (mc/find-one coll {:games {$all games}} [:_id])))

(defn from-games
  "Get an expert from the database by id."
  ([games fields] (mc/find-one-as-map coll {:games {$all games}} fields))
  ([games] (from-id id [])))

(defn- relationship-to-object
  "Converts the given relationship to an object which can be stored in mongo."
  [relationship]
  relationship)

(defn add-many
  "Add an expert with the given id to the datastore."
  [game-relationships]
  (mc/insert-batch coll (map relationship-to-object game-relationships)))
