(ns micromonkeys.cheater
  (:require [micromonkeys.main :as stem]
            [micromonkeys.rest :as rest]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:gen-class))

(defn groom
  "Grooming behavior for the Cheater monkey: always say no!"
  [_]
  (log/warn "Someone want to be groomed, but I'm too lazy for that..")
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body   (json/encode {:message "Screw you!"})})

(def cheater-micromonkey
  (rest/make-app (rest/override-route {"groom" {:post groom}})))

(defn -main []
  (stem/-main #'cheater-micromonkey))
