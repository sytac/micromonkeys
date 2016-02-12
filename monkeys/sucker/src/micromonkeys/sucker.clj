(ns micromonkeys.sucker
  (:require [micromonkeys.main :as stem]
            [micromonkeys.rest :as rest]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:gen-class))


(defn groom
  "Grooming behavior for the Sucker monkey: always say yes"
  [_]
  (log/warn "Someone asked for grooming, I love grooming!")
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body   (json/encode {:message "Always happy to groom another monkey!"})})

(def sucker-micromonkey
  (rest/make-app (rest/override-route {"groom" {:post groom}})))

(defn -main []
  (stem/-main #'sucker-micromonkey))
