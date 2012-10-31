(ns board-ultimatum.views.expert
  "Namespace containing all expert views including login, logout, selection and
  rating."
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.flash :as flash]
            [board-ultimatum.form-validators :as valid]
            [board-ultimatum.engine.model.expert :as expert]
            [noir.session :as sess]
            [noir.validation :as vali]
            [noir.response :as resp])
  (:use [noir.core :only [defpage defpartial pre-route render]]
        [board-ultimatum.session]
        [hiccup core element]
        [clojure.walk :only [keywordize-keys]]
        [hiccup.form :only [form-to label text-field submit-button]]))

(pre-route [:any "/expert/*"] {:as req}
           (when-not (expert-logged-in?)
             (flash/put! :warning "You must log in before accessing this
                                  functionality.")
             (resp/redirect "/expert")))

;; Partial used by the GET /expert route when there is an expert logged in.
(defpartial expert-logged-in []
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

;; Partial used by the GET /expert route when there is not an expert logged in.
(defpartial expert-not-logged-in []
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
  "Takes a collection of games and returns a grid-cols by grid-rows 2-D vector."
  [games]
  {:pre [(= (count games) grid-size)]
   :post [(= (count %) grid-rows) (= (count (first %)) grid-cols)]}
  (for [y (range grid-rows)]
    (for [x (range grid-cols)]
      (nth games (+ (* y grid-rows) x)))))

(defpartial game-thumb [{:keys [bgg_id name thumbnail]}]
  [:div {:id (str "game-" bgg_id)
         :class (str "game-container span" (/ 12 grid-cols))
         :data-toggle "button"}
   [:div.game
    [:input {:type "hidden" :name (str "games[" bgg_id "]") :value "false"}]
    [:div.image-wrapper
     [:img.img-rounded {:src thumbnail}]]
    [:div.title-wrapper
     [:h5 name]]]])

(defpartial grid-row [coll]
  [:div.row-fluid
   (map game-thumb coll)])

(defpartial expert-page [& body]
  (common/with-javascripts (cons "/js/expert.js" common/*javascripts*)
    (common/layout
      body)))

;; A page show to the expert
(defpage "/expert/select" []
  (expert-page
    [:div.page-header
     [:h1 "Select all of the games you are familiar with"]]
    [:form#expert-select {:method "post"}
     (map grid-row (games-to-grid (expert/games-for (current-expert-id) grid-size)))
     [:div.form-actions
      [:div.row-fluid
       [:button#main-button.btn.btn-large.span8
        [:strong "I am unfamiliar with all of these games. Next!"]]
       [:a.btn.btn-large.span4 {:href "/expert"}
        "I'm done with this for today."]]]]))

(defpartial expert-compare [ids]
  (expert-page [:p (str (seq ids))]))

(defpage [:post "/expert/select"] {:keys [games]}
  (let [selected-ids (map (fn [x] (Integer/parseInt (first x)))
                          (filter (fn [[_ selected]]
                                    (= selected "true"))
                                  games))]
    (if (empty? selected-ids)
      (resp/redirect "/expert/select")
      (expert-compare selected-ids))))
