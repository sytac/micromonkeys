(defproject grudger-monkey "0.1.26-SNAPSHOT"
  :description "An experiment with microservices and monkey populations. This projects implements a monkey that will never groom those who refused to groom it before."
  :url "http://sytac.io"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [stem-monkey "0.1.2-SNAPSHOT"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-midje "3.1.3"]]

  :lein-release {:deploy-via    :lein-install
                 :build-uberjar true}

  :profiles {:dev {:dependencies [[midje "1.6.0"]]}}

  :main micromonkeys.grudger
  :aot [micromonkeys.grudger])
