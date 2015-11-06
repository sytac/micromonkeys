## Grudging monkeys and microservices: the Grudger
##
## author Carlo Sciolla <carlo.sciolla@sytac.io>
##
###################################################

FROM clojure/jdk8
MAINTAINER Carlo Sciolla <carlo.sciolla@sytac.io>

# Prepare the app root folder
RUN mkdir -p /usr/local/monkeys
WORKDIR /usr/local/monkeys

# Download the dependencies AOT
COPY project.clj /usr/local/monkeys/
RUN lein deps

# Package the app
COPY . /usr/local/monkeys
RUN mv "$(LEIN_SNAPSHOTS_IN_RELEASE=true lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" grudger.jar

# Run the app
CMD ["java", "-Dmonkey=grudger", "-Dmonkey-id=1", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500/", "-jar", "grudger.jar"]