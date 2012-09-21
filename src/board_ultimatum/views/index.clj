(ns board-ultimatum.views.index
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/" []
         (common/layout
           [:h1 "Welcome to board-ultimatum"]
           [:p "The board-ultimatum is a board game recommendation system" 
               [:br] 
               "This system is implemented in Clojure"]))
