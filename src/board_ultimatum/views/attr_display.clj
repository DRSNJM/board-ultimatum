(ns board-ultimatum.views.attr-display
  (:use [hiccup.element]
        [hiccup.form]
        [noir.core]))

(defn pretty-hours [length]
  (let [hours (/ (float length) 60)]
    (if (== hours (int hours))
      (str (int hours) " hours")
      (str hours " hours"))))

(defn game-length [length]
  (cond
   (>= length 120) (pretty-hours length)
   :else (str length " minutes")))

(defn num-players [min-players max-players]
  (cond
   (= min-players max-players) (str max-players " player")
   :else (str min-players "-" max-players " players")))

(defn format-score [score]
  (format "%+.1f" (float score)))

(defn split-by-n
  "Returns a lazy sequence of n successive items from coll while
  (pred item) returns true. pred must be free of side-effects."
  [n coll]
  (lazy-seq
   (let [s (seq coll)
              front (take n s)]
       (when (not-empty s)
         (cons front (split-by-n n (drop n s)))))))

(defn n-groups [n coll]
  (split-by-n (/ (count coll) n) coll))

(defn columns [n coll]
  (apply interleave (n-groups n coll)))

(defpartial colored-attr [value freq color]
  [:span value
   [:span {:style (str "color: " color ";")}
    " (" freq ")"]])

(defn format-freq [value freq]
  (cond
   (> freq 150) (colored-attr value freq "#007FCF")
   (> freq 100) (colored-attr value freq "#B751C2")
   (> freq 60)  (colored-attr value freq "#D97C75")
   :else        (colored-attr value freq "#CFA176")))

;; Build 3-state preference selection buttons
(defpartial build-tri-state [{:keys [value frequency description]} attr]
  [:div {:style "float:left;margin:10px 20px 0px 0px;"}
   [:div {:class (clojure.string/join " " ["btn-group" "tri-state" value])}
    [:button {:type "button"
              :class "btn btn-mini btn-danger"}
     [:i {:class "icon-thumbs-down"}]]
    [:button {:type "button"
              :data-title "More Info"
              :data-content description
              :class "btn btn-mini option"}
     (format-freq value frequency )]
    [:button {:type "button"
              :class "btn btn-mini btn-success"}
     [:i {:class "icon-thumbs-up"}]]]
   [:input {:type "hidden" :name (str attr "[" value "]") :value "0"}]])

;; Build radio preference selection buttons
(defpartial build-radio-buttons [name-value form-name]
  [:div
    [:div {:class "btn-group radio-buttons" :data-toggle "buttons-radio"}
      (map
        #(identity [:button {:type "button" :value (val %) :class "btn"} (key %)])
        name-value)]
    [:input {:type "hidden" :name form-name :value ""}]])

(defpartial player-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div {:class (str "icon player p" (first num))}]
    (check-box "num-players[]" false num)
    [:div.bottom-label (str num " Players")]]])

(defpartial time-checkboxes [num]
  [:div.selection
   [:label.checkbox
    [:div {:class (str "icon time t" num)}]
    (check-box "length[]" false num)
    [:div.bottom-label (game-length num)]]])
