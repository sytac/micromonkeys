(ns micromonkeys.rest
  (:require [ring.util.response     :as resp]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json   :refer [wrap-json-response
                                            wrap-json-params]]
            [bidi.ring :refer (make-handler)]
            [micromonkeys.clock :as clock]
            [micromonkeys.clock.state :as clock-state]
            [micromonkeys.bugs :as bugs]
            [cheshire.core :as json]
            [micromonkeys.config :as config]
            [clojure.tools.logging :as log]))

(defn response
  "Returns a ring response"
  [status body]
  (-> (resp/response body)
      (resp/status status)))

(defn clock-response
  "Processes a clock tick request and sends a Ring response back"
  [tick]
  (let [olderror (clock/last-error)
        newclock (clock/process-tick tick)]
    (if (= olderror (clock/last-error))
      (response 200 newclock)
      (response 400 (clock/last-error)))))

(defprotocol AsNumber
  "Coerces a value to a number"
  (str->int [i]))

(extend-protocol AsNumber

  String
  (str->int [^String s]
    (try
      (Integer/parseInt s)
      (catch NumberFormatException ex
        (log/warn "Wrong tick value: " s)
        Long/MIN_VALUE)))

  Long
  (str->int [i] i)

  Integer
  (str->int [i] i))

(defn process-clock
  "Processes a clock tick PUT request"
  [req]
  (if-let [tick (get-in req [:params "tick"])]
    (if-let [tick (str->int tick)]
      (clock-response tick)
      (response 400 "Bad request, an invalid clock tick was received"))
    (response 400 "Bad Request, a numeric clock tick was expected")))

(defn show-clock
  "Shows the current clock tick"
  [_]
  (response 200 @clock-state/clock))

(declare micromonkey)

(defn api-index
  "Ring handler that discloses all the monkey's API"
  [req]
  {:status 200
   :body (json/encode micromonkey)})

(defn emit-config
  "Transforms the current configuration into a JSON"
  []
  (json/encode @config/config))

(defn show-config
  "Ring handler that dumps the current configuration as JSON"
  [req]
  {:status 200
   :body (emit-config)
   :headers {"Content-Type" "application/json"}})

(defn will-groom
  "Replies to grooming requests"
  [_]
  {:status 501
   :body (json/encode {:message "This monkey didn't evolve any grooming behavior"})
   :headers {"Content-Type" "application/json"}})

(defn add-bugs
  "Adds the terrible bugs to the monkey"
  [_]
  (bugs/bugs!))

(defn remove-bugs
  "Removes the terrible bugs from the monkey"
  [_]
  (bugs/remove-bugs!))

(defn resuscitate
  "Resuscitates the monkey, if dead"
  [_]
  (bugs/resuscitate))

(defn status
  "Shows the status for this monkey"
  [_]
  (let [bugs (if (bugs/bugs?)
               {:bugs true
                :infested-since (bugs/infestation-age)}
               {:bugs false})]
    (merge {:status 200
            :body (json/encode {:clock @clock-state/clock
                                :bugs bugs
                                :dead (bugs/dead?)})
            :headers {"Content-Type" "application/json"}} bugs)))

(def stem-micromonkey
  ["/" {""       api-index
        "clock"  {:put process-clock
                  :get show-clock}
        "bugs" {:put add-bugs
                :delete remove-bugs}
        "status" status
        "groom" {:post will-groom}
        "reborn" {:post resuscitate}
        "config" show-config}])

(defn override-route
  "Overrides a route to be served according to the provied bidi spec. Returns the new routes."
  [spec]
  (let [routes (second stem-micromonkey)]
    ["/" (merge routes spec)]))

(defn make-app
  "Makes a Monkey app out of a Bidi dispatch map by applying the proper middleware"
  [handler]
  (-> handler
      make-handler
      wrap-params
      wrap-json-response
      wrap-json-params))

(def app
  (make-app stem-micromonkey))
