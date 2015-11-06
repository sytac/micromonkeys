(ns micromonkeys.consul
  (:require [micromonkeys.consul.api :as api]
            [micromonkeys.config :as cfg]
            [org.httpkit.client :as http]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [timeout go-loop <! go >! chan alts!]]))

(defonce service-status (atom :healthy))

(defonce health-changed (chan))

(defn set-healthy
  "Sets the monkey as healthy"
  []
  (reset! service-status :healthy)
  (go (>! health-changed :healthy)))

(defn set-warn
  "Sets the monkey as in working status"
  []
  (reset! service-status :warn)
  (go (>! health-changed :warn)))

(defn set-fail
  "Sets the monkey dead"
  []
  (reset! service-status :fail)
  (go (>! health-changed :fail)))

(defn set-skip
  "Sets the monkey to ignore requests"
  []
  (reset! service-status :skip)
  (go (>! health-changed :skip)))

(defn extract-seqnum
  "Given a service named foobar1 extracts the number 1"
  [service]
  (let [[_ id] (re-matches #".*(\d+)" (get service "ServiceID"))]
    (if id
      (Integer/parseInt id)
      -1)))

(defn die
  "This monkey is dead!"
  [& _]
  (System/exit -1))

(defn status->checkfn
  "Decides what to tell Consul depending on the current service status"
  []
  (get {:healthy api/mark-healthy
        :warn    api/mark-warn
        :fail    die
        :skip    :do-nothing} @service-status))

(defn refresh-status
  "Tells Consul of the current status of this node"
  [service-id]
  ((status->checkfn) (cfg/consul-endpoint) service-id))

(defn service-checks-loop
  "Continuously tell Consul of the current status of this node"
  [service-id]
  (refresh-status service-id) ; don't wait the timeout the first time
  (go-loop []
           (let [_  (alts! [(timeout 9000)  ; the TTL in Consul are hard coded to 10s
                            health-changed])]
             (log/info "Refreshing the state to" @service-status)
             (refresh-status service-id)
             (recur))))

(defn next-id
  "Finds the next sequential number to register a new node for the given service"
  [service]
  (let [nodes (api/list-nodes (cfg/consul-endpoint) service)
        next  (inc (reduce max 0 (map extract-seqnum nodes)))]
    next))

(defn add-monkey
  "Adds a service to Consul and schedules its health checks. The service ID is
   automatically generated as service X where X is a number computed by looking
   at the already nodes registered for this service, adding 1"
  [service]
  (let [id (str service (or (cfg/config-for :monkey-id) (next-id service)))]
    (when (api/register-service (cfg/consul-endpoint)
                                service
                                id
                                (Integer/parseInt (cfg/config-for :monkey-port)))
      (service-checks-loop id)
      id)))

(defn random-from
  "Selects a random element from the provided collection"
  [c]
  (when (seq c)
    (nth c (int (rand (count c))))))

(defn monkey->groom-url
  "Builds a URL to ask a monkey to groom us"
  [monkey]
  (let [addr (get monkeey "ServiceAddress") #_"192.168.59.103:"
        port ":8080" #_(get monkey "ServicePort")]
    (str "http://"  addre port "/groom")))

(defn ask-grooming
  "Asks another monkey to groom us"
  [monkey]
  (let [monkey-url (monkey->groom-url monkey)
        body {:whoami (cfg/whoami)}
        _ (log/warn "Asking" monkey-url "for grooming")
        {:keys [status body error] :as response} @(http/post monkey-url {:body body})
        _ (log/warn monkey-url "said" response)]
    (if (= 200 status)
      {:groomer (get monkey "ServiceID")}
      {:groomer (get monkey "ServiceID")
       :refused true
       :message body
       :status status})))

(defn find-groomer
  "Finds a groomer and asks if he's willing to groom us"
  []
  (let [categories ["sucker" "grudger" "cheater"]
        monkeys (mapcat #(api/list-nodes (cfg/consul-endpoint) %) categories)
        candidate (random-from monkeys)]
    (ask-grooming candidate)))

(defn register
  "Registers this monkey as a service in Consul"
  []
  (if-let [me (cfg/whoami)]
    (add-monkey me)
    (log/error "Cannot register a service with no name")))
