(ns board-ultimatum.engine.model.expert
  "A namespace for maniuplating the experts as part of the datastore"
  (:require [board-ultimatum.views.common :as common]
            [noir.session :as sess]
            [noir.validation :as vali]
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
  (if (exists? id)
    (vali/set-error :identity "Another expert is already using that identity.")
    (mc/insert coll {:identifier id})))

(defn current-id
  "Get the logged in expert's id from the session."
  [] (sess/get :expert-id))

(defn logged-in?
  "Determine if there is an expert logged in right now."
  [] (boolean (current-id)))

(defn logout
  "Logout the currently logged in expert."
  [] (sess/remove! :expert-id))

(defn login
  "Login the given expert."
  [id]
  (if (exists? id)
    (sess/put! :expert-id id)
    (vali/set-error :identity "No user with that identity exists. Try
                         registering.")))

(def ^:private attempt-action-to-function
  "A mapping of attempt actions to functions. The sign up action maps to adding
  an expert to the database and the Log In action simply logs in the given id."
  {"Register" add "Log In" login})

(defn process-attempt
  "Process the given attempt by either creating a new user or logging them in."
  [{:keys [action identity]}]
  ((get attempt-action-to-function action) identity))
