(ns micromonkeys.clock.state
  (:require [clojure.core.async :refer [go chan >! >!! <! mult tap go-loop]]))

(defonce clock (atom {:tick       0
                      :last-error nil}))

(defonce clock-channel (chan 5))

(defonce multiplex (mult clock-channel))

(defmacro watch
  "Executes code as soon as a new clock tick is there. This anaphoric macro exposes
   the _tick symbol that can be used in the provided code to access the current tick
   number."
  [& code]
  `(let [t# (tap multiplex (chan 5))]
     (go-loop []
              (let [~'_tick (<! t#)]
                ~@code
                (recur)))))
