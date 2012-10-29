(ns board-ultimatum.flash
  "Wrap noir's flash functionality with some helper functions to simplify the
  default use case of an alert flash."
  (:refer-clojure :exclude [get])
  (:require [noir.session :as sess]))

(defn- a-put!
  "Helper method to public put!. Wraps around flash-put!."
  [t m] (sess/flash-put! :alert {:type t :message (apply str m)}))

(defn put!
  "A simple wrapper around noir's built-in flash put. Takes a variable number of
  aguments. If the first argument is a keyword then it is assumed to be the type
  of alert.  Otherwise the type is set to :info."
  [flash-type & message]
  (println message)
  (if (keyword? flash-type)
    (a-put! flash-type message)
    (a-put! :info (cons flash-type message))))

;; Alias this namespace's get to noir's flash-get.
(def get (partial sess/flash-get :alert))
