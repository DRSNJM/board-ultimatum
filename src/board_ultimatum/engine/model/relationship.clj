(ns board-ultimatum.engine.model.relationship
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [board-ultimatum.engine.model.expert :as expert])
  (:use [monger.operators]))

(def coll
  "The name of the collection on mongo containing relationships."
  "relationships")

(def rating-span
  "The difference in the span of possible rating values."
  4)

(def rating-offset
  "The value at which the lowest rating starts."
  1)

(defn exists?
  "An relationship between the two games exists in the database."
  [games] (boolean (mc/find-one coll {:games {$all games}} [:_id])))

(defn from-games
  "Get an expert from the database by id."
  ([games fields] (mc/find-one-as-map coll {:games {$all games}} fields))
  ([games] (from-games games [])))

(defn average-ratings
  "Run aggregate to calculate the average rating for each relationship."
  []
  (mc/aggregate coll [{$group {:_id "$games" :rating {$avg "$rating"}}}]))

(defn convert-to-object
  "Convert the given relationship to an object to be stored in the datastore.
  The first argument must be an expert map contain an object id."
  [{expert-obj-id :_id} [games rating]]
  {:expert expert-obj-id
   ;; Always sort games on insert so we do not have [a b] and [b a] entries.
   :games (sort games)
   :rating (float (/ (- rating rating-offset) rating-span))})

(defn add-many
  "Add the given relationships to the datastore and keep track for the expert."
  [game-relationships expert-id]
  (mc/insert-batch coll (map (partial convert-to-object
                                      (expert/from-id expert-id [:_id]))
                             game-relationships)))

(defn pairs-seen-by
  "Return a sequence of pairs already seen by the given expert."
  [expert-id]
  (set (map :games
            (mc/find-maps coll
                          {:expert (:_id (expert/from-id expert-id))}
                          [:games]))))
