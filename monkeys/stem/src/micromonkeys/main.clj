(ns micromonkeys.main
  (:require [ring.adapter.jetty :as jetty]
            [micromonkeys.rest  :as monkey]
            [micromonkeys.config :as config]
            [micromonkeys.consul :as consul]
            [clojure.tools.logging :as log])
  (:import [org.eclipse.jetty.server Server]))

(defonce server (atom nil))

(defn kill
  "Stops a currently running server, if not nil"
  [^Server server]
  (when server
    (.stop server)))

(defn boot
  "Runs a jetty server. Accepts a parameter to enable composition with kill."
  [handler]
  (jetty/run-jetty handler {:port  8080
                            :join? false}))

(defn init
  "Initializes the service"
  []
  (config/reload)
  (consul/register))

(defn start
  "Starts an embedded jetty and lets handler serve requests. Stops the server if
   it's already running, then starts it again."
  [handler port]
  (init)
  (swap! server #(do (kill %)
                     (boot handler))))

(defn stop
  "Stops the server if it's still running"
  []
  (swap! server kill))

(defn -main
  "Starts an embedded Jetty and attaches a monkey to it"
  [handler]
  (if-let [port (config/config-for :monkey-port)]
    (start handler port)
    (log/error "No port configured, please add -Dmonkey-port to the startup command.")))
