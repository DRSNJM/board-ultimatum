(ns board-ultimatum.views.expert
  "Namespace containing all expert views including login, logout, selection and
  rating."
  (:require [board-ultimatum.views.common :as common]
            [board-ultimatum.flash :as flash]
            [board-ultimatum.form-validators :as valid]
            [noir.validation :as vali]
            [noir.response :as resp])
  (:use [noir.core :only [defpage defpartial pre-route render]]
        [board-ultimatum.session]
        [hiccup core element]
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

;; Construct a list of game titles and cover images
(def retrieved-games
  ; Currently produces dummy values, will eventually retrieve random games from
  ; database
  (for [y (range grid-rows)]
    (for [x (range grid-cols)]
      [(+ (* y grid-rows) x)
       "Game Title: Might Be a Bit Long"
       "http://placehold.it/150x100"])))

(defpartial game-thumb [[game-id game-name imgURL]]
  [:div {:id (str "game-" game-id)
         :class (str "game-container span" (/ 12 grid-cols))
         :data-toggle "button"}
   [:div.game
    [:input {:type "hidden" :name (str "game-field-" game-id) :value "off"}]
    [:img.img-rounded {:src imgURL}]
    [:h5 game-name]]])

(defpartial grid-row [coll]
  [:div.row-fluid
   (map game-thumb coll)])

;; A page show to the expert
(defpage "/expert/select" []
  (common/with-javascripts (cons "/js/expert.js" common/*javascripts*)
    (common/layout
      [:div.page-header
       [:h1 "Select all of the games you are familiar with"]]
      [:form#expert-select {:method "post"}
       (map grid-row retrieved-games)
       [:div.form-actions
        [:div.row-fluid
         [:button.btn.btn-large.span8
          [:strong "I am unfamiliar with all of these games. Next!"]]
         [:a.btn.btn-large.span4 {:href "/expert"}
          "I'm done with this for today."]]]])))

(defpage [:post "/expert/select"] {:as m}
  (render "/expert/select"))
