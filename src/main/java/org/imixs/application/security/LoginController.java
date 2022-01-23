/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.application.security;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.imixs.jwt.HMAC;
import org.imixs.jwt.JWSAlgorithm;
import org.imixs.jwt.JWTBuilder;
import org.imixs.jwt.JWTException;

/**
 * The LoginController validates a given api secret and generates an Admin Token
 * to be used to access the application and Rest API.
 * 
 * @author rsoika
 * 
 */
@Named
@RequestScoped
public class LoginController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LoginController.class.getName());

	private String token;
	private String secret;

	public LoginController() {
		super();
	}

	public String login() throws JWTException {
		
		return generateToken();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	
	public boolean isAuthenticated() {
		return false;
	}

	/**
	 * Helper Method generating a JWT token
	 * @return
	 * @throws JWTException
	 */
	private String generateToken() throws JWTException {
		String token=null;
		// generate a admin payload
		String payload = "{\"sub\":\"admin\",\"displayname\":\"Administrator\",\"groups\":[\"user\",\"yyy\"]}";

		logger.info("...generating new access token for " + payload);
		
		
	//	try {
			// We need a signing key...
			SecretKey secretKey = HMAC.createKey(JWSAlgorithm.JDK_HS256, secret.getBytes());

			JWTBuilder builder;

			builder = new JWTBuilder().setKey(secretKey).setHeader(JWTBuilder.DEFAULT_HEADER).setPayload(payload)
					.sign();

			token= builder.getToken();
			logger.info("token=" + builder.getToken());
//		} catch (JWTException e) {
//			e.printStackTrace();
//		}
			
			// set bearer token
//			FacesContext.getCurrentInstance()
//            .getExternalContext()
//            .getRequestMap()
//            .put("Authorization","Bearer "+token);

			
			// set cookie
			setCookie("jwt",token, (int) TimeUnit.HOURS.toSeconds(1));
			
		//return "/pages/welcome.xhtml?jwt="+token;
		
		return "/pages/welcome.xhtml";

	}

	
	/**
	 * Helper method to set a cookie
	 * @param name
	 * @param value
	 * @param expiry
	 */
	public void setCookie(String name, String value, int expiry) {

	    FacesContext facesContext = FacesContext.getCurrentInstance();

	    HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
	    Cookie cookie = null;

	    Cookie[] userCookies = request.getCookies();
	    if (userCookies != null && userCookies.length > 0 ) {
	        for (int i = 0; i < userCookies.length; i++) {
	            if (userCookies[i].getName().equals(name)) {
	                cookie = userCookies[i];
	                break;
	            }
	        }
	    }

	    if (cookie != null) {
	        cookie.setValue(value);
	    } else {
	        cookie = new Cookie(name, value);
	        cookie.setPath(request.getContextPath());
	    }

	    cookie.setMaxAge(expiry);

	    HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
	    response.addCookie(cookie);
	  }

	/**
	 * helper method to read a cookie
	 * @param name
	 * @return
	 */
	  public Cookie getCookie(String name) {

	    FacesContext facesContext = FacesContext.getCurrentInstance();

	    HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
	    Cookie cookie = null;

	    Cookie[] userCookies = request.getCookies();
	    if (userCookies != null && userCookies.length > 0 ) {
	        for (int i = 0; i < userCookies.length; i++) {
	            if (userCookies[i].getName().equals(name)) {
	                cookie = userCookies[i];
	                return cookie;
	            }
	        }
	    }
	    return null;
	  }
}
