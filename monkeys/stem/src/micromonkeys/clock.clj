(ns micromonkeys.clock
  (:require [clojure.core.async :refer [go chan >! >!! <! mult tap go-loop]]
            [micromonkeys.bugs :as bugs])
  (:use micromonkeys.clock.state))

(defn already-processed
  "Produces an error message that describes an already processed tick"
  [tick]
  {:message (str "The clock tick was already processed: " tick)
   :time (System/currentTimeMillis)})

(defn process-clock
  "Processes a clock tick, handling conflict resolution:
   - clock ticks with a seq number <= of the current monkey clock are ignored
   - clock ticks with a seq number > of the current monkey clock are processed"
  [old tick]
  (condp = (compare (:tick old) tick)
    -1 (do (>!! clock-channel tick) (merge old {:tick tick}))
     0 (merge old {:last-error (already-processed tick)})
     1 (merge old {:last-error (already-processed tick)})))

(defn currently-dead
  "Produces an error message that describes a clock beign ignored as the monkey is dead"
  [tick]
  {:message (str "This monkey is dead, the following tick was ignored: " tick)
   :time (System/currentTimeMillis)})

(defn update-clock
  "Sets a new value of the clock tick"
  [old tick]
  (if (bugs/dead?)
    (merge old {:last-error (currently-dead tick)})
    (process-clock old tick)))

(defn process-tick
  "Processes the new tick coming from the clock"
  [tick]
  (swap! clock update-clock tick))

(defn last-error
  "Reads the last processing error from the current clock or the provided clock"
  ([]      (last-error @clock))
  ([clock] (:last-error clock)))
