(ns board-ultimatum.views.welcome
  (:require [board-ultimatum.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to board-ultimatum"]))
