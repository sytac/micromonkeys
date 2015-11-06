(ns micromonkeys.bugs
  (:require [micromonkeys.clock.state :as clock]
            [micromonkeys.consul :as consul]))

(defonce bugs (atom {:infested false}))

(defn bugs?
  "True if the monkey has bugs"
  []
  (true? (:infested @bugs)))

(defn bugs!
  "Adds the bugs to the monkey"
  []
  (reset! bugs {:infested true
                :started  (:tick @clock/clock)})
  (consul/set-warn))

(defn remove-bugs!
  "Removes the bugs from the monkey"
  []
  (reset! bugs {:infested false})
  (consul/set-healthy))

(defn infestation-age
  "Returns the number of clock ticks since the infestation started"
  []
  (if (bugs?)
    (let [current (:tick @clock/clock)
          start (or (:started @bugs) current)]
      (- current start))))

(defn dead?
  "The monkey is dead if the infestation started more than 2 clock ticks ago"
  []
  (< 2 (or (infestation-age) -1)))

(defn groom
  "Removes the bugs"
  []
  (when (not (dead?))
    (reset! bugs {:infested false})))

(defn infested?
  "True if the monkey has bugs but is not dead yet"
  []
  (and (bugs?)
       (not (dead?))))

(defn resuscitate
  "Resuscitates the monkey"
  []
  (when (dead?)
    (reset! bugs {:infested false})))

(defn ask-grooming
  "Asks another monkey for grooming"
  []
  (let [groomer (consul/find-groomer)]
    (if (:refused groomer)
      groomer
      (do (groom)
          groom))))

(add-watch clock/clock
           :apply-bugs
           (fn [key ref old new]
             (println "Applying clock")
             (cond
              (dead?)     (consul/set-fail)
              (infested?) (do (consul/set-warn)
                              (ask-grooming))
              :else       (consul/set-healthy))))
