## Grudging monkeys and microservices: the Grudger
##
## author Carlo Sciolla <carlo.sciolla@sytac.io>
##
###################################################

FROM clojure
MAINTAINER Carlo Sciolla <carlo.sciolla@sytac.io>

# Prepare the app root folder
RUN mkdir -p /usr/local/monkeys
WORKDIR /usr/local/monkeys

# Download the dependencies AOT
COPY target/grudger-monkey-0.1.26-SNAPSHOT-standalone.jar /usr/local/monkeys/

# Package the app
RUN mv grudger-monkey-0.1.26-SNAPSHOT-standalone.jar grudger.jar

# Run the app
CMD ["java", "-Dmonkey=grudger", "-Dmonkey-id=1", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500/", "-jar", "grudger.jar"]
