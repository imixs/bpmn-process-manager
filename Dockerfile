FROM quay.io/wildfly/wildfly:26.0.0.Final
COPY ./target/*.war /opt/jboss/wildfly/standalone/deployments/

# JASPI module
COPY ./docker/configuration/modules/ /opt/jboss/wildfly/modules/
# adapt stanalone configuraiton
COPY ./docker/configuration/standalone-imixs.xml /opt/jboss/wildfly/standalone/configuration/standalone-imixs.xml
# Run with microprofiles
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c","standalone-imixs.xml"]