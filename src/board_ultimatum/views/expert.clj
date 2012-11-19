(ns board-ultimatum.views.expert
  "Namespace containing all expert views including login, logout, selection and
  rating."
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.flash :as flash]
            [board-ultimatum.form-validators :as valid]
            (board-ultimatum.engine.model [relationship :as relationship]
                                          [expert :as expert])
            [board-ultimatum.engine.model :as model]
            [clojure.math.combinatorics :as combo]
            [clojure.string :as string]
            [noir.session :as sess]
            [noir.validation :as vali]
            [noir.response :as resp])
  (:use [noir.core :only [defpage defpartial pre-route render]]
        [board-ultimatum.session]
        [hiccup core element]
        [clojure.walk :only [keywordize-keys]]
        [hiccup.form :only [select-options form-to label text-field submit-button]]))

(pre-route [:any "/expert/*"] {:as req}
           (when-not (expert-logged-in?)
             (flash/put! :warning "You must log in before accessing this
                                  functionality.")
             (resp/redirect "/expert")))

(defpartial expert-logged-in
  "Used by the GET /expert route when there is an expert logged in."
  []
  [:div.page-header
   [:h1 "You have two choices"]]
  [:ol#expert-choices.row-fluid
   [:li.span6
    [:div.hero-unit.small
     [:h2 "Select games you know "
      [:small "and tell us how good of a recommendation they are for
              eachother."]]
     (link-to {:class "btn btn-primary btn-large"}
              "/expert/select" [:strong "Do some work!"])]]
   [:li.span6
    [:h2 "&hellip;or be boring" [:small " and log out."]]
    (link-to {:class "btn btn-large"}
             "/expert/logout" "Log out")]])

(defpartial expert-not-logged-in
  "Used by the GET /expert route when there is not an expert logged in."
  []
  [:div.page-header
   [:h1 "Welcome, Board Game Expert!"]]
  [:p "Since this is not a security required application there is no
      strong authentication. However, to keep track of your history of
      recommendations we still need to identify you."]
  [:p "The resulting system is very simple. As an expert you will use the
      same identifier each time you use the application. Your identifier
      can be anything you want including your name or random string of
      numbers."]
  [:div.well
   (form-to {:id "expert-login" :class "form-inline"} [:post "/expert"]
            (text-field {:id "identity" :placeholder "Your identifier"}
                        "identity") " "
            (submit-button {:name "action" :class "btn btn-primary"}
                           "Log In") " "
            (submit-button {:name "action" :class "btn"}
                           "Register"))])

;; The route from which an expert should start at. If they are not logged in
;; they can here. If they are then they are redirected to select.
(defpage "/expert" []
  (common/layout
    (if (expert-logged-in?)
      (expert-logged-in)
      (expert-not-logged-in))))

;; POST version of the /expert route. This route processes the login/register
;; attempt and redirects back to the GET page.
(defpage [:post "/expert"] {:as attempt}
  (when-not (expert-logged-in?)
    (when (valid/attempt? attempt)
      (process-login-attempt attempt))
    (if-let [errors (vali/get-errors :identity)]
      (flash/put! :error (common/format-errors errors))))
  (resp/redirect "/expert"))

;; Logout the currently logged in expert.
(defpage "/expert/logout" []
  (expert-logout)
  (resp/redirect "/expert"))

;; grid-cols must divide 12 for use with CSS grid system.
(def grid-cols 4)
(def grid-rows 3)
(def grid-size (* grid-cols grid-rows))

(defn games-to-grid
  "Takes a collection of games and returns a 2-D vector of games where each row
  is of length grid-cols."
  [games]
  (let [num-games (count games)
        num-rows (int (Math/ceil (/ num-games grid-cols)))
        remainder (mod num-games grid-cols)
        last-row-size (if (= remainder 0) grid-cols remainder)]
    (for [y (range num-rows)]
      (for [x (range (if (= (dec num-rows) y) last-row-size grid-cols))]
        (nth games (+ (* y grid-cols) x))))))

(defpartial game-thumb
  "Takes a map representing a game and returns markup for a game including its
  title and thumbnail."
  [{:keys [bgg_id name thumbnail]}]
  [:div {:id (str "game-" bgg_id)
         :class (str "game-container span" (/ 12 grid-cols))
         :data-toggle "button"}
   [:div.game
    [:input {:type "hidden" :name (str "games[" bgg_id "]") :value "false"}]
    [:div.image-wrapper
     [:img.img-rounded {:src thumbnail}]]
    [:div.title-wrapper
     [:h5 name]]]])

(defpartial grid-row
  "Create a fluid row with the content being game-thumb mapped over the given
  coll."
  [coll]
  [:div.row-fluid
   (map game-thumb coll)])


;; A page show to the expert
(defpage "/expert/select" []
  (common/with-javascripts (cons "/js/expert.js" common/*javascripts*)
    (common/layout
      [:div.page-header
       [:h1 "Select all of the games you are familiar with"]]
      [:form#expert-select {:method "post"}
       (map grid-row (games-to-grid (expert/games-for (current-expert-id)
                                                      grid-size)))
       [:div.form-actions
        [:div.row-fluid
         [:button#main-button.btn.btn-large.span8
          [:strong "I am unfamiliar with all of these games. Next!"]]
         [:a.btn.btn-large.span4 {:href "/expert"}
          "I'm done with this for today."]]]])))

(defpartial compare-game
  "Similar to game-thumb but for the compare page. Taks a bgg_id and returns
  markup containing a thumbnail and title of the referenced game."
  [bgg-id]
  (let [{:keys [name thumbnail]} (model/get-game-by-id bgg-id
                                                       [:name :thumbnail])]
    [:div.game.span4
     [:div.image-wrapper
      [:img.img-rounded {:src thumbnail}]]
     [:div.title-wrapper
      [:h5 name]]]))

(defpartial compare-games
  "Take an index and pair of games returning markup an expert can use to rate
  the recommendation quality of the pair."
  [index [game-a game-b]]
  [:div.row-fluid {:id (str "rate-games" index)}
   [:div.rate-games
    (compare-game game-a)
    [:div.rating.span4 {:id (str "rating" index)}
     [:div.rating-slider {:id (str "rating-slider" index)}
      [:select (select-options
                 [["Bad" 1] ["" 1.5] ["Poor" 2] ["" 2.5] ["OK" 3] ["" 3.5]
                  ["Good" 4] ["" 4.5] ["Great" 5]] 3)]]]
    (compare-game game-b)]])

(defpartial expert-compare
  "The main body of /expert/compare when ids is greater than 1. Provides an
  interface for rating the quality of each game pair combination."
  [ids]
  (common/with-javascripts (concat common/*javascripts*
                                   ["/js/jquery-ui-slider.min.js"
                                    "/js/selectToUISlider.jQuery.min.js"
                                    "/js/expert-compare.js"])
    (common/layout
      [:div.page-header
       [:h1 "Rate these recommendations"]
       [:div.instructions
        [:p "For each pair below please rate how good of a recommendation each
            game is given the other."]]
       [:div#expert-compare
        (form-to [:post "/expert/compare"]
          (map-indexed compare-games
                       (shuffle (map shuffle (combo/combinations ids 2))))
          [:div.form-actions
           [:div.row-fluid
            [:button#main-button.btn.btn-large.span6.btn-primary
             [:strong "I'm done rating these games."]]
            [:a.btn.btn-large.span6 {:href "/expert/select"}
             "I can't rate these games."]]])]])))

(defn- input-game-filter
  "For a given map entry return whether the value is the string \"true\"."
  {:test (fn []
           (assert (input-game-filter ["123" "true"]))
           (assert (not (input-game-filter ["123" "false"]))))}
  [[_ selected?]]
  (= selected? "true"))

(defn- input-game-mapper
  "For a given two element return the first element parsed as an integer."
  {:test (fn []
           (assert (= 123 (input-game-mapper ["123" "true"]))))}
  [[bgg-id _]]
  (Integer/parseInt bgg-id))

(defn- convert-input-games
  "Convert input from the from to an easy to handle two element vector where the
  first element is a vector of selected ids and the second is a vector of."
  {:test (fn [] (assert (= (convert-input-games
                             {"123" "true" "234" "false" "567" "true"})
                           [[123 567] [234]])))}
  [games]
  [(map input-game-mapper (filter input-game-filter games))
   (map input-game-mapper (filter (complement input-game-filter) games))])

;; Take selected games from an expert and if they selected 2 or more render an
;; interface for comparing them.
(defpage [:post "/expert/select"] {:keys [games]}
  (let [[selected-ids unfamiliar-ids] (convert-input-games games)]
    (expert/add-unfamiliar-games (current-expert-id) unfamiliar-ids)
    (if (<= (count selected-ids) 1)
      (resp/redirect "/expert/select")
      (expert-compare selected-ids))))

;; This route specifies how to take the results of recommendation quality
;; ratings provided by an expert.
(defpage [:post "/expert/compare"] {:as relationships}
  (when-not (empty? relationships)
    (relationship/add-many
      (into {} (map (fn [[pair-str value]]
                      [(map #(Integer/parseInt %)
                            (string/split pair-str (re-pattern "-")))
                       (Integer/parseInt value)])
                    relationships))
      (current-expert-id)))
  (resp/redirect "/expert/select"))
