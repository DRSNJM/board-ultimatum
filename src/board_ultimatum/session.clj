(ns board-ultimatum.session
  "Functions for manipulating the session.  Specifally, login and logout."
  (:require [noir.session :as sess]
            [noir.validation :as vali]
            [board-ultimatum.engine.model.expert :as expert]
            [board-ultimatum.flash :as flash]))

(defn current-expert-id
  "Get the logged in expert's id from the session."
  [] (sess/get :expert-id))

(defn expert-logged-in?
  "Determine if there is an expert logged in right now."
  [] (boolean (current-expert-id)))

(defn expert-logout
  "Logout the currently logged in expert."
  [] (sess/remove! :expert-id))

(defn- expert-login
  "Login the given expert."
  [id]
  (if (expert/exists? id)
    (do
      (sess/put! :expert-id id)
      (flash/put! :success "Hello, " id ". You have logged in successfully!"))
    (vali/set-error :identity "No user with that identity exists. Try
                         registering.")))

(defn- expert-register
  "Attempt to add the expert to the datastore if it does not already exist."
  [id]
  (if (expert/exists? id)
    (vali/set-error :identity "Another expert is already using that identity.")
    (do
      (expert/add id)
      (flash/put! :success "Thanks for registering " id ". Try logging in." ))))

(def ^:private attempt-action-to-function
  "A mapping of attempt actions to functions. The sign up action maps to adding
  an expert to the database and the Log In action simply logs in the given id."
  {"Register" expert-register "Log In" expert-login})

(defn process-login-attempt
  "Process the given attempt by either creating a new user or logging them in."
  [{:keys [action identity]}]
  ((get attempt-action-to-function action) identity))
