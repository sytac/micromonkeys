(ns wilderness.handler
  (:require [compojure.core :refer [GET POST PUT DELETE defroutes routes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [cheshire.core :as json]
            [wilderness.consul :as consul]
            [wilderness.clock :as clock]
            [wilderness.monkeys :as monkeys]))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app]
     (include-js "js/app.js")]]))

(defn list-monkeys
  "Lists the nodes registered in consul for the given service name"
  ([]
     {:body (json/encode (consul/monkey-nodes))
      :headers {"Content-Type" "application/json"}})
  ([service]
     {:body (json/encode (consul/service-nodes service))
      :headers {"Content-Type" "application/json"}}))

(defn clock-tick
  "Sends the clock a signal to advance. Causes all monkeys to receive the tick"
  []
  {:body (json/encode (clock/tick))
   :headers {"Content-Type" "application/json"}})

(defn get-clock
  "Retrieves the current value of the clock"
  []
  {:body (json/encode {:tick @clock/clock})
   :headers {"Content-Type" "application/json"}})

(defn add-bugs
  "Adds bugs to a monkey"
  [id]
  (if-let [success (monkeys/add-bugs id)]
    {:status 200
     :body "true"}
    {:status 500
     :body "false"}))

(defn remove-bugs
  "Removes bugs from a monkey"
  [id]
  (if-let [success (monkeys/remove-bugs id)]
    {:status 200
     :body "true"}
    {:status 500
     :body "false"}))

(defroutes site-routes*
  (GET "/" [] home-page)
  (resources "/")
  (not-found "Not Found"))

(defroutes api-routes*
  (GET "/monkeys" [] (list-monkeys))
  (GET "/monkeys/:service" [service] (list-monkeys service))

  (PUT    "/monkeys/:service/:id/bugs" [service id] (add-bugs id))
  (DELETE "/monkeys/:service/:id/bugs" [service id] (remove-bugs id))

  (POST "/tick" [] (clock-tick))
  (GET  "/tick" [] (get-clock)))

(def api-middleware
  (merge api-defaults {:security {:anti-forgery false}}))

(def site-routes (wrap-defaults site-routes* site-defaults))
(def api-routes  (wrap-defaults api-routes*  api-middleware))

(consul/register)

(def app
  (let [handler (routes api-routes site-routes)]
    (if (env :dev) (wrap-exceptions handler) handler)))
