(ns board-ultimatum.views.common
  (:require [clojure.string :as string]
            [board-ultimatum.flash :as flash])
  (:use [noir.core :only [defpartial]]
        [hiccup.core]
        [hiccup.element]
        [hiccup.page :only [include-js include-css html5]]))

;; ## Macros

;; Set a dynamicly bindable default site title.
(def site-title "Board Ultimatum")
(def ^:dynamic *site-title* site-title)

(defmacro with-title
  "Specify what site title to use dynamically."
  [title & body]
  `(binding [*site-titel ~title]
     ~@body))

;; Set a dynamicly bindable default vector of js files to include.
(def ^:dynamic *javascripts* ["/js/bootstrap.min.js"])

(defmacro with-javascripts
  "Specify what javascripts to use dynamically."
  [js-paths & body]
  `(binding [*javascripts* ~js-paths]
     ~@body))

;; ## Helper Functions

(defn alert
  "Displays an alert box."
  ([class type message show-close?]
   [:div#flash {:class (str "alert fade in alert-" (name class))}
    (when show-close?
      [:a.close {:data-dismiss "alert"} "&times;"])
    [:strong (if (keyword? type)
               (string/capitalize (name type))
               type) " "] message])
  ([class type message] (alert class type message true))
  ([type message] (alert type type message true)))

(defpartial format-errors
  "Takes a collection of error messages and formats it into html."
  [errs]
  (when (seq errs) [:div.help-block [:ul (map #(html [:li %]) errs)]]))

;; ## Layouts

;; Base layout used by the web app.
(defpartial base-layout [& content]
  (html5
    [:head
     [:title *site-title*]
     ; Meta Tag Necessary for Twitter Boostrap
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1.0"}]
     [:link {:rel "shortcut icon" :type "image/x-icon" :href "/favicon.ico"}]
     (include-css "/css/bootstrap.min.css")
     ; Get jQuery
     (include-js "/js/jquery-1.7.2.min.js")]
    [:body
     content
     (html (map include-js *javascripts*))]))

;; Standard layout used by most pages on the website.
(defpartial layout [& content]
  (base-layout
    [:div#navbar.navbar.navbar-fixed-top
     [:div.navbar-inner
      [:div.container-fluid
       [:a.btn.btn-navbar
        {:data-toggle "collapse" :data-target ".nav-collapse"}
        [:span.icon-bar] [:span.icon-bar] [:span.icon-bar]]
       [:a.brand.dropdown-toggle {:href "/"} site-title]
       [:div.nav-collapse
        [:ul.nav
         [:li (link-to "/recommend" "Recommend")]
         [:li (link-to "/similar" "Similar Games")]]
        [:ul.nav.pull-right
         [:li (link-to "/expert" "Experts")]
         [:li.divider-vertical]
         [:li [:a
               {:href "http://drsnjm.github.com/board-ultimatum"}
               "Documentation"]]
         [:li [:a
               {:href "https://github.com/DRSNJM/board-ultimatum"}
               "Source"]]]]]]]
    [:div#main-wrapper
     [:div#main.container-fluid
      (when-let [{t :type c :class m :message} (flash/get)]
        (alert (if (nil? c) t c) t m))
      content]
     [:footer#footer.container-fluid
      [:a.label.label-success {:href "http://drsnjm.github.com/about/"} "About"]
      " &copy; 2012 " (link-to "http://drsnjm.github.com/" "DRSNJM")]]))
