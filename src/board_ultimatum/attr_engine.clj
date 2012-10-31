(ns board-ultimatum.attr_engine
	(:require [clojure.set :as set])
	(:use [clojure.set]))

; This namespace contains methods for scoring games

; Partition tags by subtype
(defn sort-tags [game]
	(apply merge-with union
		(map 
			#(identity 
				{(keyword (:subtype %)) #{(keyword (:value %))}})
			(:tags game))))

(defn num-players-score [attrs game]

	)

(defn mechanics-score [attrs tags]
	; TODO Check mechanics was active
	(apply + 
		(map 
			#(if (contains? (:mechanic tags) (keyword (key %)))
				(* (Integer/parseInt (val %)) 20)
				0)
			(:mechanics attrs))))

(defn weight-score [attrs game]
	)

(defn score-game [attrs game]
	(let [tags (sort-tags game)]
		(merge 
			game
			{:score (+
				(mechanics-score attrs tags))})))

(defn score-games [attrs games]
	(map #(score-game attrs %) games))