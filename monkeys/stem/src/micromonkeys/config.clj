(ns micromonkeys.config
  (:require [clojure.core.async :refer [go-loop <! timeout chan go pipe >! alts!]]
            [micromonkeys.consul.api :as consul]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log]))

(defn monkey?
  "Predicate for strings that start with some substring"
  [s]
  (.startsWith (name s) "monkey"))

(defn only-monkeys
  "Extract only relevant configuration from the env"
  [env]
  (let [monkeys (filter monkey? (keys env))]
    (select-keys env monkeys)))

(defn init
  "Fetches the initial configuration."
  []
  {:env (merge {:config-reload 1000} (only-monkeys env))})

; Initial configuration, updated every ::config-reload millis
(defonce config (atom (init)))

(defonce reload-control (chan))

(defn config-for
  "Retrieves a configuration item. If the configuration is not found in Consul,
   then the environment is inspected."
  [key]
  (if-let [value (get-in @config [:consul key])]
    value
    (get-in @config [:env key])))

(defn whoami
  "Retrieves the current monkey identity from the configuration"
  []
  (config-for :monkey))

(defn consul-config
  "Reads the configuration from Consul"
  [consul]
  (if-let [monkey (str "monkeys/" (whoami))]
    (consul/prop consul monkey)))

(defn merge-config
  "Merges the consul configuration into the online one"
  [consul-config]
  (swap! config merge {:consul consul-config}))

(defn consul-endpoint
  "Retrieves the Consul endpoint from the configuration"
  []
  (config-for :monkey-consul-endpoint))

(defn reload
  "Reloads the configuration"
  []
  (if-let [consul (consul-endpoint)]
    (if-let [config (consul-config consul)]
      (do (log/debug "Found counfiguration for the monkey " (whoami))
          (merge-config config))
      (log/warn "No counfiguration found for the monkey " (whoami)))))

(defn stop-reload
  "Pauses the configuration reload loop"
  []
  (log/info "Stopping the Consul configuration reload for " (whoami))
  (go (>! reload-control ::stop)))

(defn start-reload
  "Resumes the configuration reload loop"
  []
  (log/info "Restarting the Consul configuration reload for " (whoami))
  (go (>! reload-control ::start)))

(defn wait-start
  "Creates a channel that will only accept a ::start message from the
   reload-config-control channel"
  []
  (pipe reload-control (chan 1 (filter #{::start}))))

(defn should-stop?
  "Returns true if the control channel asked the reload process to stop"
  [value channel]
  (and (= reload-control channel)
       (= ::stop value)))

; Configuration is continuously fetched
(defn reload-loop
  "Continuosly call reload if the reload flag is up"
  []
  (log/info "Starting the configuration reload loop")
  (go-loop []
           (let [[v c] (alts! [reload-control
                               (timeout (config-for :config-reload))])]
             (cond
              (should-stop? v c) (do (<! (wait-start))
                                     (recur))
              :else              (do (log/debug "Reloading the Consul configuration")
                                     (reload)
                                     (recur))))))
