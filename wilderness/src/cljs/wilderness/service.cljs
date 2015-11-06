(ns wilderness.service
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.session :as session]))

(defn update-monkeys
  "Sets the new monkeys in the current session"
  [monkeys]
  (session/put! :monkeys monkeys))

(defn list-monkeys
  "Lists the monkeys from the server"
  []
  (go (let [response (<! (http/get "/monkeys"))]
        (if (:success response)
          (update-monkeys (:body response))))))

(defn add-bugs!
  "Adds bugs to a monkey"
  [monkey]
  (go (<! (http/put (str "/monkeys/" (:service monkey) "/" (:id monkey) "/bugs")))
      (list-monkeys)))

(defn remove-bugs!
  "Removes bugs from a monkey"
  [monkey]
  (go (<! (http/delete (str "/monkeys/" (:service monkey) "/" (:id monkey) "/bugs")))
      (list-monkeys)))

(defn bugs!
  "Commands a monkey to get or remove bugs. Accepts a string, either +bugs or -bugs"
  [command monkey]
  (case command
    "+bugs" (add-bugs! monkey)
    "-bugs" (remove-bugs! monkey)))
