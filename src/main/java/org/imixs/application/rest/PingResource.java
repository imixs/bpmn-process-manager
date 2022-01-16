package org.imixs.application.rest;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.bpmn.manager.keystore.SetupService;
 
/**
 *
 * @author rsoika
 */
@Path("ping")
public class PingResource {
	private static Logger logger = Logger.getLogger(PingResource.class.getName());

	@Inject
	@ConfigProperty(name = "mail.host", defaultValue = "host@mail.com")
	private String mail_host;

	@Inject 
	SetupService setupService;
	
	
	
	@GET
	public String ping() {
		logger.info("...ping.... env mail.host=" + mail_host);

		return "Ping JakartaEE 9 " + System.currentTimeMillis();
	}

}
