#!/bin/bash

JAVA_OPTS="-Dmonkey=sucker-development -Dmonkey-port=8081 -Dmonkey-consul-endpoint=http://192.168.59.103:8500" lein run
