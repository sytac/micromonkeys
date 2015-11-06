(defproject stem-monkey "0.1.2-SNAPSHOT"
  :description "An experiment with microservices and monkey populations. This project is a base, undifferentiated monkey."
  :url "http://sytac.io"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [bidi "1.19.0"]
                 [ring/ring-json "0.3.1"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [environ "1.0.0"]
                 [cheshire "5.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-core "2.3"]]

  :plugins [[lein-midje "3.1.3"]
            [skuro/lein-release "2.0.0"]]

  :lein-release {:deploy-via :lein-install}

  :profiles {:dev {:dependencies [[midje "1.6.0"]]}})
