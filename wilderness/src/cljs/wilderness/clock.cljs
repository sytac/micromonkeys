(ns wilderness.clock
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.session :as session]
            [wilderness.service :as service]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! chan timeout alts! close!]]))

(defonce clock-controls (chan 5))

(defn valid?
  "True if the tick is a number or a sting representatino of a number"
  [tick]
  (<= 0 (js/parseInt (get tick :tick))))

(defn update-clock
  "Stores a new clock value in the session"
  [tick]
  (if (valid? tick)
    (do (session/assoc-in! [:clock :tick] (:tick tick))
        (service/list-monkeys))
    (prn "Invalid tick:" tick)))

(defn clock-request
  "Sends a request to the clock"
  [method]
  (go (let [response (<! (method "/tick"))]
        (if (:success response)
          (update-clock (:body response))))))

(defn tick
  "Advances the clock of one unit"
  []
  (clock-request http/post))

(defn fetch
  "Retrieves the current clock value"
  []
  (clock-request http/get))

(defn stop
  "Stops the clock from running continuously"
  []
  (when-let [stop-chan (session/get-in [:clock :stop-chan])]
    (close! stop-chan))
  (session/put! :clock (dissoc (session/get :clock) :stop-chan))
  (session/put! :clock (dissoc (session/get :clock) :status)))

(defn playing?
  "Returns true if the clock is ticking away"
  []
  (= :play (session/get-in [:clock :status])))

(defn play
  "Plays the clock every second"
  []
  (when (not (playing?))
    (let [stop-chan (chan)]
      (session/assoc-in! [:clock :status] :play)
      (session/assoc-in! [:clock :stop-chan] stop-chan)
      (go-loop [one-sec (timeout 1000)]
               (let [[_ c] (alts! [one-sec stop-chan])]
                 (if (= stop-chan c)
                   (stop)
                   (do (tick)
                       (recur (timeout 1000)))))))))

(go-loop []
         (let [event (<! clock-controls)]
           (case event
             :tick (tick)
             :play (play)
             :stop (stop))
           (recur)))
