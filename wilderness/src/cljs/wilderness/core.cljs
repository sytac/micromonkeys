(ns wilderness.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [wilderness.monkeys :as monkeys]
            [wilderness.service :as service]
            [wilderness.clock :as clock]
            [cljs.core.async :refer [>!]]
            [figwheel.client :as fw])
  (:import goog.History))

(enable-console-print!)

;; -------------------------
;; Initialization

(fw/watch-and-reload
 :websocket-url   "ws://localhost:3449/figwheel-ws"
 :jsload-callback
 (fn []
   (println "reloaded")))

(service/list-monkeys)

(clock/fetch)

;; -------------------------
;; Views

(defn control-panel
  "Draws the clock controls"
  []
  [:div.controls
   [:div [:a {:href "#"
              :on-click #(go (>! clock/clock-controls :tick))} "tick(1)"]]
   [:div [:a {:href "#"
              :on-click #(go (>! clock/clock-controls :play))} "-play->>"]]
   [:div [:a {:href "#"
              :on-click #(go (>! clock/clock-controls :stop))} "-stop-| |"]]
   (when-let [status (session/get-in [:clock :status])]
     [:div [:span (name status)]])
   [:div.current-clock [:span [:h3 (session/get-in [:clock :tick])]]]])

(defn home-page []
  [:div [:h1 "Meet the Micromonkeys"]
   [control-panel]
   (doall (for [monkey (session/get :monkeys)]
            ^{:key monkey} [monkeys/draw monkey]))])

(defn about-page []
  [:div [:h2 "About wilderness"]
   [:div [:a {:href "#/"} "go to the home page"]]])

#_(monkeys/blink :sucker)
#_(monkeys/blink :grudger)
#_(monkeys/blink :cheater)

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
