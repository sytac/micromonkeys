## Grudging monkeys and microservices: the Simulator
##
## author Carlo Sciolla <carlo.sciolla@sytac.io>
##
####################################################

FROM clojure/jdk8
MAINTAINER Carlo Sciolla <carlo.sciolla@sytac.io>

# Prepare the app root folder
RUN mkdir -p /usr/local/wilderness
WORKDIR /usr/local/wilderness

# Download the dependencies AOT
COPY project.clj /usr/local/wilderness/
RUN lein deps

# Package the app
COPY . /usr/local/monkeys
RUN mv "$(LEIN_SNAPSHOTS_IN_RELEASE=true lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" wilderness.jar

# Run the app
CMD ["java", "-jar", "wilderness.jar"]
