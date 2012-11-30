(ns board-ultimatum.views.index
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage pre-route]]
        [noir.response :only [redirect]]
        [hiccup.element]))

(pre-route "/" {}
           (redirect "/recommend"))
