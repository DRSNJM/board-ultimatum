(ns board-ultimatum.engine
    (:refer-clojure :exclude [==])
    (:use [clojure.core.logic]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defrel num-players game n)

(facts num-players [['Checkers 2]
                    ['Chess 2]
                    ['Solitaire 1]
                    ['Sorry 4]
                    ['Yahtzee 6]
                    ['Nothing 0]])

(defrel property* game p)

(facts property* [['Solitaire 'has-cards]
                  ['Sorry 'has-dice]
                  ['Yahtzee 'has-dice]
                  ['Nothing 'has-everything]])

(defrel category game type)

(facts category [['Checkers 'strategy]
                 ['Chess 'strategy]
                 ['Solitaire 'card]
                 ['Sorry 'chance]
                 ['Yahtzee 'chance]])

(defn query []
    "Returns the number of players of Checkers"
    (run* [q]
        (num-players 'Checkers q)))

(defn query2 []
    "Returns all games with 2 players"
    (run* [q]
        (num-players q 2)))

(defn query3 [value tolerance]
    "Returns all games with 'value' players with +/- 'tolerance'"
    (run* [q]
      (fresh [n]
        (conde [(membero n (range (- value tolerance) (+ value tolerance 1)))])
        (num-players q n))))

(defn query4 []
    "Returns all games with the property 'has-dice'"
    (run* [q]
        (property* q 'has-dice)))

(defn query5 []
    "Return all games with 4 players that have dice and are 
    also are in the category 'chance' games"
    (run* [q]
        (num-players q 4)
        (property* q 'has-dice)
        (category q 'chance)))
