#!/bin/sh
mvn clean package && docker build -t imixs/bpmn-processmanager .
docker rm -f bpmn-processmanager || true && docker run -p 8080:8080 --name bpmn-processmanager imixs/bpmn-processmanager 
