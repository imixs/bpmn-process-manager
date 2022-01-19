
## JASPI Configuraiton

Find details here:

 - https://docs.wildfly.org/18/WildFly_Elytron_Security.html#Elytron_and_Java_Authentication_SPI_for_Containers-JASPI
 - https://blogs.nologin.es/rickyepoderi/index.php?/archives/182-JASPI-authentication-in-elytron.html
 

## Configuration

The Imixs-JWT Lib is added as a Wildfly Module via the Dockerfile. The wildfly module configuration can be find in /docker/configuration/modules. 


To configure this module in Wildfly we add simply the following entries into the wildfly configuration (standalone-imixs.xml):

            <!-- Imixs JWT jaspi configuration -->
            <jaspi>
                <jaspi-configuration name="imixs-jwt-configuration"  description="Imixs JWT Configuration">
                    <server-auth-modules>
                        <server-auth-module class-name="org.imixs.jwt.jaspic.JWTAuthModule" module="org.imixs.jwt.jaspic" flag="REQUIRED">
                            <options>
                                <property name="secret" value="secret"/>
                                <property name="expire" value="60"/>
                            </options>
                        </server-auth-module>
                    </server-auth-modules>
                </jaspi-configuration>
            </jaspi>


The options should be ajusted to your needs.

In addtion the `application-security-domains` are reconfigured as shown below:


            <application-security-domains>
            	<!-- activate jaspi module configured before -->
                <application-security-domain name="other" security-domain="ApplicationDomain" enable-jaspi="true" integrated-jaspi="false"/>
            </application-security-domains>


With this setup in a applicaiton there is no more need for a extra security realm. You only need to add the security-constraints in your web-xml according to your applicaiton roles. 

That's it!


## How to generate Subsystms by the wildfly cli tool

You can create a elytron/jaspi subsystem also within a running server with the jboss-cli.sh tool. See the following instructions

First connect into the bash of the running Docker / Wildfly container. 

Now you cann run the jboss-cli tool and connect to your server:


	$ ./wildfly/bin/jboss-cli.sh 
	You are disconnected at the moment. Type 'connect' to connect to the server or 'help' for the list of supported commands.
	[disconnected /] connect
	
Now you can run your subsystem command adding a new jspi module:

	
	./subsystem=elytron/jaspi-configuration=imixs-jwt-configuration:add( \
        description="Imixs JWT Configuration", \
        server-auth-modules=[{class-name=org.imixs.jwt.jaspic.JWTAuthModule, module=org.imixs.jwt.jaspic, flag=REQUIRED, options={secret=secret, expire=60}})


The result can be seen in the standalone.xml file - XML Example:


    <jaspi>
        <jaspi-configuration name="imixs-jwt-configuration"  description="Imixs JWT Configuration">
            <server-auth-modules>
                <server-auth-module class-name="org.imixs.jwt.jaspic.JWTAuthModule" module="org.imixs.jwt.jaspic" flag="REQUIRED">
                    <options>
                        <property name="secret" value="secret"/>
                        <property name="expire" value="60"/>
                    </options>
                </server-auth-module>
            </server-auth-modules>
        </jaspi-configuration>
    </jaspi>
            



To add the jaspic module manually you can run:


	module add --name=org.imixs.jwt.jaspic --resources=/opt/jboss/wildfly/imixs-jwt-1.1.0.jar --dependencies=javax.servlet.api,javaee.api,javax.security.auth.message.api

This will copy the 

finally exit:

	$ exit
            