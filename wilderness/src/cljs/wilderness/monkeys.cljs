(ns wilderness.monkeys
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [cljs.core.async :refer [chan timeout <!]]
            [wilderness.service :as service])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn log
  "Logs in the console"
  [& msg]
  (. js/console log (apply str msg)))

(defn flip
  "Flips the eyes status of a monkey"
  [id]
  (let [next (case (session/get-in [id :eyes])
               :closed :open
               :open   :closed)]
    (session/assoc-in! [id :eyes] next)
    next))

(defn time-for
  "Decides how much time to wait"
  [eyes]
  (case eyes
    :closed 200
    :open   (max (int (rand 5000)) 1000)
    :nil    (max (int (rand 5000)) 1000)))

(defn eyes
  "Gets the status of the eyes of a monkey"
  [id]
  (session/get-in [id :eyes]))

(defn eyes!
  "Sets the status of the eyes of a monkey"
  [id status]
  (session/assoc-in! [id :eyes] status))

(defn blinking?
  "Verifies if a monkey is already blinking its eyes"
  [id]
  (session/get-in [id :blinking]))

(defn stop-blinking
  "Cancels the blinking process for the given monkey"
  [id]
  (when-let [t (blinking? id)]
    (js/clearTimeout t)
    (let [current (session/get-in [id])
          new (dissoc current :blinking)]
      (session/assoc-in! [id] new))))

(defn blink
  "Blinks the eyes of a monkey"
  [id]
  (when (not (blinking? id))
    (letfn [(->open [id ->next]
              (eyes! id :open)
              (let [proc (js/setTimeout #(->next id ->open) (time-for :open))]
                (eyes! id :open)))
            (->closed [id ->next]
              (eyes! id :closed)
              (let [proc (js/setTimeout #(->next id ->closed) (time-for :closed))]
                (eyes! id :closed)))]
      (->open id ->closed))))

(defn monkey-health
  "Draws a box describing the monkey health"
  [m]
  (let [health (:health m)]
    (case health
      "dead"    [:div.health.dead]
      "healthy" [:div.health.healthy]
      [:div.health.buggy])))

(defn monkey-clock
  "Shows at what clock the monkey has arrived"
  [m]
  [:div.clock [:span [:h3 (:clock m)]]])

(defn monkey-bug
  "Button to add bugs to a monkey"
  [m]
  (let [text (case (:health m)
               "healthy" "+bugs"
               "-bugs")]
    [:div.clock [:span [:h4 [:a {:on-click #(service/bugs! text m)
                                 :href "#"} text]]]]))

(defn monkey
  "Draws a monkey surrounding context, placing the picture inside"
  [m picture]
  [:div.monkey (if (= "dead" (:health m)) {:class "dead"}) picture
        [:div.status [:h2 (:id m)]
         [:div.stats
          [monkey-health m]
          [monkey-clock m]
          [monkey-bug m]]]])

(defn sucker
  "Draws a sucker"
  [m]
  (monkey m (case (eyes (:id m))
              :closed [:div.sucker.closed]
              [:div.sucker])))

(defn cheater
  "Draws a cheater"
  [m]
  (monkey m (case (eyes (:id m))
              :closed [:div.cheater.closed]
              [:div.cheater])))

(defn grudger
  "Draws a grudger"
  [m]
  (monkey m (case (eyes (:id m))
              :closed [:div.grudger.closed]
              [:div.grudger])))

(def monkey-kinds
  {"sucker"  sucker
   "grudger" grudger
   "cheater" cheater})

(defn draw
  "Draws a monkey"
  [monkey]
  (-> (:service monkey)
      monkey-kinds
      (apply [monkey])) )
