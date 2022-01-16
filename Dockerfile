FROM jboss/wildfly:25.0.0.Final
COPY ./target/*.war /opt/jboss/wildfly/standalone/deployments/
