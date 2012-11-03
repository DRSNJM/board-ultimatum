(ns board-ultimatum.engine.model.relationship
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model.expert :as expert])
  (:use [monger.operators]))

(def coll
  "The name of the collection on mongo containing relationships."
  "relationships")

(def rating-increment-count
  "The number of increments the rating slider uses."
  1000)

(defn exists?
  "An relationship between the two games exists in the database."
  [games] (boolean (mc/find-one coll {:games {$all games}} [:_id])))

(defn from-games
  "Get an expert from the database by id."
  ([games fields] (mc/find-one-as-map coll {:games {$all games}} fields))
  ([games] (from-games games [])))

(defn convert-to-object
  "Convert the given relationship to an object to be stored in the datastore.
  The first argument must be an expert map contain an object id."
  [{expert-obj-id :_id} [games rating]]
  {:expert expert-obj-id
   ;; Always sort games on insert so we do not have [a b] and [b a] entries.
   :games (sort games)
   :rating (float (/ rating rating-increment-count))})

(defn add-many
  "Add the given relationships to the datastore and keep track for the expert."
  [game-relationships expert-id]
  (mc/insert-batch coll (map (partial convert-to-object
                                      (expert/from-id expert-id [:_id]))
                             game-relationships)))
