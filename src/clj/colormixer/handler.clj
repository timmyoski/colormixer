(ns colormixer.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [colormixer.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
     [:div.wrapper
      [:h3 {:name "test"
            :style {:height 3}}
       "...compiling your colormix..."]
      [:img {:src "/images/favicon.ico"
             :class "loading-img"}]]])
              ;;:style {:width "400px"}}]]])

      ;; some hiccup syntax isn't working here bc of the html f()??
      ;; [:img {:src "/images/favicon.ico" :style "width: 200px;"} "hmmm"]]])
      ;; [:img {:src "/images/favicon.ico" :style {:width "200px"}} "hmmm2"]]])

;; <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/> <!--320-->

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"}]
   [:title "color-mix"]
   [:link {:rel "icon" :href "/images/favicon.ico"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
