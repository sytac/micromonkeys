## Grudging monkeys and microservices: the Sucker
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
COPY target/sucker-monkey-0.1.26-SNAPSHOT-standalone.jar .

# Package the app
RUN mv sucker-monkey-0.1.26-SNAPSHOT-standalone.jar sucker.jar

# Run the app
CMD ["java", "-Dmonkey=sucker", "-Dmonkey-id=1", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500/", "-jar", "sucker.jar"]
