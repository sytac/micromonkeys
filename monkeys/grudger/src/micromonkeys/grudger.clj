(ns micromonkeys.grudger
  (:require [micromonkeys.main :as stem]
            [micromonkeys.rest :as rest]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:gen-class))

; at the beginning, we only don't trust monkeys we cannot recognize
(defonce grudge (atom #{:unknown}))

(def do-groom {:status 200
               :headers {"Content-Type" "application/json"}
               :body   (json/encode {:message "Always happy to groom another monkey!"})})

(def no-groom {:status 400
               :headers {"Content-Type" "application/json"}
               :body (json/encode {:message "I hate you"})})

(defn groom?
  "Decides where to groom or not"
  [monkey]
  (if (@grudge monkey)
    (do (log/info monkey "asked for grooming, but I hold a grudge on him!")
        no-groom)
    (do (log/info monkey "asked for grooming, I'd love to groom him!")
        do-groom)))

(defn req->monkey
  "Extracts the identity of a monkey requesting grooming"
  [grooming-req]
  (if-let [monkey (-> (:body grooming-req)
                      slurp
                      (json/decode keyword)
                      :whoami)]
    monkey
    :unknown))

(defn groom
  "Grooming behavior for the Grudger monkey: only groom if no grudge"
  [req]
  (groom? (req->monkey req)))

(defn show-grudge
  "Ring handler that exposes the names of the monkeys we hold a grudge upon"
  [_]
  {:status 200
   :body (json/encode {:grudge @grudge})
   :headers {"Content-Type" "application/json"}})

(def grudger-micromonkey
  (rest/make-app (rest/override-route {"groom" {:post groom}
                                       "grudge" show-grudge})))

(defn -main []
  (stem/-main #'grudger-micromonkey))
