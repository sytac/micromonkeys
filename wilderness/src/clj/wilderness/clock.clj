(ns wilderness.clock
  (:require [wilderness.consul :as consul]
            [org.httpkit.client :as http]))

(def clock (atom 0))

(defn healthy?
  "True if the monkey is healtny"
  [monkey]
  (= :healthy (:health monkey)))

(defn send-tick!
  "Sends a tick signal to a monkey"
  [{:keys [endpoint id] :as monkey}]
  @(http/put (str endpoint "/clock") {:query-params {:tick @clock}}))

(defn tick
  "Sends a tick message to all monkeys after advancing the clock one tick forward"
  []
  (let [monkeys (consul/monkey-nodes)]
    (swap! clock inc)
    (doall (map send-tick! monkeys))
    {:tick @clock}))
