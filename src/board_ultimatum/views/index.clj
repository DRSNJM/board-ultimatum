(ns board-ultimatum.views.index
  (:require [board-ultimatum.views.common :as common])
  (:use [noir.core :only [defpage]]
        [hiccup.element]))

(defpage "/" []
         (common/layout
           [:h1 "Welcome to board-ultimatum"]
           [:p "The board-ultimatum is a board game recommendation system" 
               [:br] 
            "This system is implemented in Clojure"]

           [:h3 "Pages"]
           [:p (link-to "/recommend" "Recommend V1")]
           [:p (link-to "/tags" "Tag Administration")]
           [:p (link-to "/expert" "Expert Interface")]))
