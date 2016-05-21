(ns micromonkeys.core
  (:require [clojure.core.async :refer [chan >! <! >!! go]]))

(defrecord Monkey [state       ; :dead | :alive | :infested
                   channel     ; used to interact with the monkey
                   last-tick]) ; last clock tick ever saw by the monkey

(defmulti apply-event :type)

(defmethod apply-event :tick
  [event monkey]
  (let [{:keys [seqnum]} (:params event)
        reply-to (:reply-to event)
        new-monkey (merge monkey {:last-tick seqnum})]
    (>!! reply-to new-monkey)))

(defn newborn
  "Creates a new Monkey"
  []
  (let [channel (chan)
        monkey  (->Monkey :alive channel -1)]
    (go (loop [monkey monkey
               event (<! channel) ]
          (recur (apply-event event monkey) (<! channel))))
    monkey))

(defn dead?
  "Returns true if the monkey is dead"
  [monkey]
  (= :dead (:state monkey)))

(defn alive?
  "Returns true if the monkey is alive"
  [monkey]
  (not (dead? monkey)))

(defn die
  "Returns a dead monkey -- ugh!"
  [monkey]
  (merge monkey {:state :dead}))

(defn infest
  "Add parasites to a monkey, poor sod"
  [monkey]
  (merge monkey {:state :infested}))

(defn infested?
  "Returns true if the monkey has bugs"
  [monkey]
  (= :infested (:state monkey)))

(defn tick
  "Receives a clock tick and propagates it to a monkey"
  [monkey seqnum]
  (let [back-chan (chan)]
    (go (>! (:channel monkey) {:type     :tick
                               :params   {:seqnum   seqnum}
                               :reply-to back-chan})
        (<! back-chan))))
