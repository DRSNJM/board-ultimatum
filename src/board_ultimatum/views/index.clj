(ns board-ultimatum.views.index
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]))

(defpage "/" []
         (common/layout
           [:p "Welcome to board-ultimatum"]))
