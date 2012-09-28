(ns board-ultimatum.views.expert
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/expert" []
         (common/layout
           [:h1 "Welcome, Board Game Expert!"]
           [:h2 "Please log in below"]))
