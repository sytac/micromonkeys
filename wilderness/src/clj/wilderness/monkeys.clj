(ns wilderness.monkeys
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [wilderness.consul :as consul]))

(defn by-id
  "Finds a monkey by id"
  [id]
  (first (filter #(= id (:id %))
                 (consul/monkey-nodes))))

(defn add-bugs
  "Adds bugs to a monkey"
  [id]
  (let [monkey (by-id id)
        endpoint (str (:endpoint monkey) "/bugs")
        {:keys [status error body] :as response} @(http/put endpoint)]
    (if (= 200 status)
      true
      (do (println "Something went wrong:" response)
          false))))

(defn remove-bugs
  "Removes bugs from a monkey"
  [id]
  (let [monkey (by-id id)
        endpoint (str (:endpoint monkey) "/bugs")
        {:keys [status error body] :as response} @(http/delete endpoint)]
    (if (= 200 status)
      true
      (do (println "Something went wrong:" response)
          false))))
