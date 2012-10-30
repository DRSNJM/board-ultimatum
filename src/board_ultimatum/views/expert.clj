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
        [hiccup core element]
        [hiccup.form :only [form-to label text-field submit-button]]))

(pre-route [:any "/expert/*"] {:as req}
           (when-not (expert/logged-in?)
             (flash/put! :warning "You must log in before accessing this
                                  functionality.")
             (resp/redirect "/expert")))

;; The route from which an expert should start at. If they are not logged in
;; they can here. If they are then they are redirected to select.
(defpage "/expert" []
  (common/layout
    [:h1 "Welcome, Board Game Expert!"]
    [:p "Since this is not a security required application there is no strong
        authentication. However, to keep track of your history of
        recommendations we still need to identify you."]
    [:p "The resulting system is very simple. As an expert you will use the same
        identifier each time you use the application. Your identifier can be
        anything you want including your name or random string of numbers."]
    [:div.well
     (form-to {:id "expert-login" :class "form-inline"} [:post "/expert"]
              (text-field {:id "identity" :placeholder "Your identifier"} "identity") " "
              (submit-button {:name "action" :class "btn btn-primary"} "Log In") " "
              (submit-button {:name "action" :class "btn"} "Register"))]))

;; POST version of the /expert route. This route processes the login/register
;; attempt and redirects back to the GET page.
(defpage [:post "/expert"] {:as attempt}
  (when-not (expert/logged-in?)
    (when (valid/attempt? attempt)
      (expert/process-attempt attempt))
    (if-let [errors (vali/get-errors :identity)]
      (flash/put! :error (common/format-errors errors))))
  (resp/redirect "/expert"))

;; Logout the currently logged in expert.
(defpage "/expert/logout" []
  (expert/logout)
  (resp/redirect "/expert"))

;; grid-cols must divide 12 for use with CSS grid system.
(def grid-cols 3)
(def grid-rows 3)
(def grid-size (* grid-cols grid-rows))

;; Construct a list of game titles and cover images
(def retrieved-games
  ; Currently produces dummy values, will eventually retrieve random games from database
  (for [x (range grid-cols)]
    (for [y (range grid-rows)]
      [(+ (* x grid-cols) y) "Game Title: Might Be a Bit Long" "http://lorempixel.com/150/100/"])))

(defpartial game-thumb [[game-id game-name imgURL]]
  [:div.span4.game {:id (str "game-" game-id)}
   [:button.btn.btn-info.btn-input {:data-toggle "button"}
    [:input {:type "hidden" :name (str "game-field-" game-id) :value "off"}]
    [:img.img-rounded {:src imgURL}]
    [:h4 game-name]]])

(defpartial grid-row [coll]
  [:div.row-fluid
   (map game-thumb coll)])

(defpage "/expert/select" []
  (common/with-javascripts (cons "/js/expert.js" common/*javascripts*)
    (common/layout
      [:h1 "Welcome, Board Game Expert!"]
      [:h2 "Please select games that you are familiar with:"]
      [:form#expert-select.container-fluid {:method "post"}
       (map grid-row retrieved-games)
       [:div.row-fluid
        [:button.btn.btn-large.btn-primary.span6 "I know these games!"]
        [:a.btn.btn-large.span6 {:href "/expert-select"} "Try the next set"]]])))

(defpage [:post "/expert/select"] {:as m}
  (str "parameters: " m))
