(ns micromonkeys.consul.api
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clojure.pprint :as pp]
            [clojure.tools.logging :as log])
  (:import [java.util Base64]
           [java.net Inet4Address]))

(defn base64->edn
  "Decodes a Base64 String into another String"
  ^String [^String base64]
  (-> (Base64/getDecoder)
      (.decode (.getBytes base64))
      (String.)
      read-string))

(defn edn->base64
  "Encodes any clojure data structure into Base64"
  [stuff]
  (let [^String stringified (with-out-str (pp/pprint stuff))]
    (-> (Base64/getEncoder)
        (.encodeToString (.getBytes stringified)))))

(defn decode-prop
  "Decodes the body of a property request"
  [body]
  (-> body
      json/decode
      first
      (get "Value")
      base64->edn))

(defn prop
  "Fetches a value associated with the given key from the consul instance running
   at the given endpoint. Ex.:

      (prop \"http://consul.example.com:8080\" \"MyKey\")"
  [consul key]

  (log/info "Getting the property " key)
  (let [{:keys [status body]} @(http/get (str consul "/v1/kv/" key))]
    (if (= 200 status)
      (decode-prop body)
      {})))

(defn list-nodes
  "Lists the nodes available for a given service"
  [consul service]
  (log/info "Getting the list of nodes for" service)
  (let [endpoint (str consul "/v1/catalog/service/" service)
        {:keys [status error body]} @(http/get endpoint)]
    (if (= 200 status)
      (json/decode body)
      (log/warn "Couldn't query available nodes for service" service ":" error))))

(defn my-ip
  "Retrieves the IP address of the host running this monkey"
  []
  (-> (Inet4Address/getLocalHost)
      (.getHostAddress)))

(defn register-service
  "Registers the current application as a service"
  [consul service id port]
  (log/info "Registering service " service " with id " id)
  (let [service {:ID id
                 :Name service
                 :Check {:TTL "10s"}
                 :Port port
                 :Address (my-ip)}
        body (json/encode service)
        endpoint (str consul "/v1/agent/service/register")
        headers {"Content-Type" "application/json"}
        {:keys [status error]} @(http/put endpoint {:body    body
                                                    :headers headers})]
    (if (= 200 status)
      true
      (log/warn "Could not register service " service " on " endpoint ": " status " " error))))

(defn send-check
  "Sends Consul the health check for this service"
  [consul id status]
  (log/debug "Sending an health check for service" id "and status" status)
  (let [check-id (str "service:" id)
        endpoint (str consul "/v1/agent/check/" status "/" check-id)
        {:keys [status error]} @(http/put endpoint)]
    (if (= 200 status)
      true
      (log/warn "Could not mark" id "as healthy on Consul:" error))))

(defn mark-healthy
  "Tells Consul this service is doing all right"
  [consul id]
  (send-check consul id "pass"))

(defn mark-warn
  "Tells Consul this service is somewhat troubled"
  [consul id]
  (send-check consul id "warn"))

(defn mark-fail
  "Tells Consul this service is failing"
  [consul id]
  (send-check consul id "fail"))
