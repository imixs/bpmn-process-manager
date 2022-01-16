#!/bin/sh
mvn clean package && docker build -t imixs/jakartaee-initializer .
docker rm -f jakartaee-initializer || true && docker run -p 8080:8080 --name jakartaee-initializer imixs/jakartaee-initializer 
