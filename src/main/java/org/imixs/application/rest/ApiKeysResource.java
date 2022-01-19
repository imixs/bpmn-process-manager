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
@Path("keys")
public class ApiKeysResource {
	private static Logger logger = Logger.getLogger(ApiKeysResource.class.getName());

	@Inject
	@ConfigProperty(name = "mail.host", defaultValue = "host@mail.com")
	private String mail_host;

	@Inject 
	SetupService setupService;
	
	
	
	@GET
	public String ping() {
		logger.info("...ping.... API Keys2" );

		return "Ping API Key Management2" + System.currentTimeMillis();
	}

}
