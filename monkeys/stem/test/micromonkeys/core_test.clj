(ns micromonkeys.core-test
  (:use midje.sweet
        micromonkeys.core)
  (:require [clojure.core.async :refer [<!!]]))

(fact "A micromonkey is alive or dead"
      (let [alive (newborn)
            dead  (die (newborn))]
        (alive? alive) => true
        (dead? alive)  => false
        (dead? dead)   => true
        (alive? dead)  => false))

(fact "Micromonkeys can get infested by bugs"
      (let [healthy  (newborn)
            infested (infest healthy)]
        (infested? healthy)  => false
        (infested? infested) => true))

(fact "I can notify the micromonkey of a new clock tick"
       (let [monkey (newborn)
             back-chan (tick monkey 42)]
         (<!! back-chan) => (fn [monkey] (= 42 (:last-tick monkey)))))
