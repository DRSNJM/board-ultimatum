(ns board-ultimatum.form-validators
  (:require [noir.session :as sess])
  (:use noir.validation))

(def ^:private attempt-actions #{"Register" "Log In"})
(defn attempt?
  "Returns whether the given expert attempt is valid or not."
  [{:keys [action identity]}]
  (rule (min-length? identity 3)
        [:identity "identity must be at least 5 characters long."])
  (rule (max-length? identity 32)
        [:identity "identity must be no more than 32 characters long."])
  (rule (re-find #"^[a-zA-z][\-_\w]*( [\-_\w]+)*\w$" identity)
        [:identity (str "identity must match this regex "
                        "/^[a-zA-z][\\-_\\w]*( [\\-_\\w]+)*\\w$/")])
  (rule (contains? attempt-actions action)
        [:identity (str "Given action (\"" action "\") is not a valid action.")])
  (not (errors? :identity)))
