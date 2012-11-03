(ns board-ultimatum.engine.model.expert
  "A namespace for maniuplating the experts as part of the datastore"
  (:refer-clojure :exclude [sort find])
  (:require [clojure.string :as string]
            [board-ultimatum.engine.model :as model]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:use [monger operators query]))

(def coll
  "The name of the collection on mongo containing experts."
  "experts")

(defn exists?
  "An expert with the given id exists in the database."
  [id] (boolean (mc/find-one coll
                             {:identifier (string/lower-case id)}
                             [:identifier])))

(defn from-id
  "Get an expert from the database by id."
  ([id fields] (mc/find-one-as-map coll
                                   {:identifier (string/lower-case id)}
                                   fields))
  ([id] (from-id id [])))

(defn name-from-id [id]
  "Get the name of the expert with the given id in its pretty form if it
  exists."
  (let [expert (from-id id [:identifier :pretty-id])]
       (get expert :pretty-id
            (get expert :identifier))))

(defn add-unfamiliar-games
  "For the given expert specify"
  [id games]
  (mc/update coll {:identifier (string/lower-case id)} {$pushAll {:unfamiliar-games games}}))

(defn games-for [id num-games]
  "Get num-games games for the given expert to compare."
  (let [unfamiliar-games (get (mc/find-one-as-map
                                coll
                                {:identifier (string/lower-case id)}
                                [:unfamiliar-games])
                              :unfamiliar-games [])]
    (with-collection "board_games"
      (find {:bgg_id {$nin unfamiliar-games}
             :random {"$near" [(rand) 0]}})
      (limit num-games))))

(defn add
  "Add an expert with the given id to the datastore."
  [id]
  (mc/insert coll {:pretty-id id
                   :identifier (string/lower-case id)}))
