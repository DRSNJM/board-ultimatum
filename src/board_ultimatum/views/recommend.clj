(ns board-ultimatum.views.recommend
  (:require [board-ultimatum.views.common :as common])
  (:require [board-ultimatum.engine :as engine])
  (:use [noir.core :only [defpage]]))

(defpage "/recommend" []
    (common/layout

        [:script {:type "text/javascript"}
          "$(document).ready(function(){
            $('.param').hide();
            
            $('#select li').click(function() {
              $(this).toggleClass('active');
              $('#input-' + $(this).attr('id')).toggle('medium', function() {
              });
              var active = $('input[name=' + $(this).attr('id') + '-active]');
              $(active[0]).attr('value', $(active[0]).attr('value') == 'false' ? 'true' : 'false');
              
            });
          });"
        ]

        [:h1 "Want a game recommendation?"]
        [:h2 "Fill in the inputs below with your preferences"]
        [:div {:class "row-fluid"}

          [:ul {:id "select" :class "span3 nav nav-pills nav-stacked"}
            [:li {:id "param1" } [:a {:href "#"} "param1"]]
            [:li {:id "param2" } [:a {:href "#"} "param2"]]
            [:li {:id "param3" } [:a {:href "#"} "param3"]]
            [:li {:id "param4" } [:a {:href "#"} "param4"]]]

          [:form {:id "game-params" :class "span9" :action "/recommend" :method "post"} 

            [:div {:id "input-param1" :class "param well well-small"}
              [:h3 "param1"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param1-active" :value "false"}]
              [:input {:type "text" :name "param1-value"}]]

            [:div {:id "input-param2" :class "param well well-small"}
              [:h3 "param2"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param2-active" :value "false"}]
              [:input {:type "text" :name "param2-value"}]]

            [:div {:id "input-param3" :class "param well well-small"}
              [:h3 "param3"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param3-active" :value "false"}]
              [:input {:type "text" :name "param3-value"}]]

            [:div {:id "input-param4" :class "param well well-small"}
              [:h3 "param4"]
              [:p "This is a description of this field"]
              [:input {:hidden "text" :name "param4-active" :value "false"}]
              [:input {:type "text" :name "param4-value"}]]

              
            [:button {:type "submit" :class "btn"} "Submit"]]
        ]))

(defpage [:post "/recommend"] {:keys [num-players]}
    (common/layout
        [:h1 "Here are your results"]
        [:h2 "Have fun playing!"]
        [:p (str "Your games: " (str (list (engine/query3 (Integer/parseInt num-players) 0))))]))