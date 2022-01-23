#!/bin/sh
mvn clean package && docker build -t imixs/bpmn-processmanager .
docker rm -f bpmn-processmanager || true &&  \
	docker run  \
	-p 8080:8080 \
	-p 8787:8787 \
	-p 9990:9990 \
	--name bpmn-processmanager imixs/bpmn-processmanager 