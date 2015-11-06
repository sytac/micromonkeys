(ns wilderness.consul
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

(def consul "http://192.168.59.103:8500")

(defn status->health
  "Translates the Consul check status into monkey health"
  [status]
  (get {"critical" :dead
        "passing"  :healthy
        "warning"  :buggy} status))

(defn service->check
  "Builds up the name of the health check of a system"
  [service]
  (let [id   (get-in service ["Service" "ID"])]
    (str "service:" id)))

(defn health
  "Extracts the health information for a given service node"
  [service]
  (let [check-name (service->check service)
        reducing (fn [health check]
                   (if (= check-name (get-in check ["CheckID"]))
                     (status->health (get-in check ["Status"]))
                     health))]
    (reduce reducing :dead (get service "Checks"))))

(defn translate-service
  "Allows the endpoints to be replaced with a system variable"
  [address]
  (or (System/getProperty "replace-endpoint")
      address))

(defn endpoint
  "Builds up the HTTP endpoint associated with the given service"
  [service]
  (let [address (translate-service (get-in service ["Node" "Address"]))
        port    (get-in service ["Service" "Port"])]
    (str "http://" address ":" port)))

(defn retrieve-clock
  "Retrieves the value of the clock as known by the provided monkey"
  [endpoint]
  (let [{:keys [status error body] :as response} @(http/get (str endpoint "/clock"))]
    (if (= 200 status)
      (let [decoded (json/decode body keyword)]
        (get decoded :tick))
      (println "Bad clock response:" response))))

(defn service->clj
  "Translates the JSON document of a Service into an understandable map"
  [service]
  (let [base-map {:service (get-in service ["Service" "Service"])
                  :id (get-in service ["Service" "ID"])
                  :endpoint (endpoint service)
                  :health (health service)}
        clock (retrieve-clock (endpoint service))]
    (if clock
      (assoc base-map :clock clock)
      base-map)))

(defn service-nodes
  "Lists the nodes associated with the provided service name"
  [service]
  (let [endpoint (str consul "/v1/health/service/" service)
        {:keys [status error body]} @(http/get endpoint)]
    (if (= 200 status)
      (map service->clj (json/decode body))
      [])))

(defn monkey-nodes
  "Lists all the monkey nodes registered in consul"
  []
  (mapcat service-nodes ["sucker" "grudger" "cheater"]))

(defn register
  "Register the wilderness into Consul"
  []
  (let [service {:Name "wilderness"}
        body (json/encode service)
        headers {"Content-Type" "application/json"}
        endpoint (str consul "/v1/agent/service/register")
        {:keys [status error]} @(http/put endpoint {:body    body
                                                    :headers headers})]
    (if (= 200 status)
      true
      (println "Couldn't register into Consul"))))
