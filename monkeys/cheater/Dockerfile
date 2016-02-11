## Grudging monkeys and microservices: the Cheater
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
COPY target/cheater-monkey-0.1.26-SNAPSHOT-standalone.jar /usr/local/monkeys/

# Package the app
RUN mv cheater-monkey-0.1.26-SNAPSHOT-standalone.jar cheater.jar

# Run the app
CMD ["java", "-Dmonkey=cheater", "-Dmonkey-id=1", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500/", "-jar", "cheater.jar"]
