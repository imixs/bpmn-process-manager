/*  
 *  Imixs-Workflow 
 *  
 *  Copyright (C) 2001-2020 Imixs Software Solutions GmbH,  
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
 *      https://www.imixs.org
 *      https://github.com/imixs/imixs-workflow
 *  
 *  Contributors:  
 *      Imixs Software Solutions GmbH - Project Management
 *      Ralph Soika - Software Developer
 */

package org.imixs.bpmn.manager.keystore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The KeystoreService EJB provides method to load and save the keystore.
 * 
 * @author rsoika
 * @version 1.0
 */
@Stateless
//@LocalBean
public class KeystoreService {

	
	public static String SETUP_OK = "OK";
	public static String MODEL_INITIALIZED = "MODEL_INITIALIZED";

	private static Logger logger = Logger.getLogger(KeystoreService.class.getName());

	@Inject
	@ConfigProperty(name = "imixs.keystore.key_algorithm", defaultValue = "RSA")
	private String keyAlgorithm;
	
	@Inject
	@ConfigProperty(name = "imixs.keystore.signature_algorithm", defaultValue = "SHA256withRSA")
	private String signatureAlgorithm;
	
	
	@Inject
	@ConfigProperty(name = "imixs.keystore.path", defaultValue = "newKeyStoreFileName.jks")
	private String keystorepath;

	@Inject
	@ConfigProperty(name = "imixs.keystore.password", defaultValue = "password")
	private String keystorepassword;

	/**
	 * This method start the system setup during deployment
	 * 
	 * @throws AccessDeniedException
	 */
	public KeyStore load() {

		KeyStore ks = null;

		logger.info("...loading java keystore...");

		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(new FileInputStream(keystorepath), keystorepassword.toCharArray());
			logger.info("...javakeystore loaded from: " + keystorepath);
			
		} catch (IOException e) {
			logger.info("...javakeystore does not yet exist - creating a new one....");
			try {
				ks.load(null, keystorepassword.toCharArray());

				// create a new root certificate
				KeyPair keyPair = createKeyPair(keyAlgorithm, 4096);
				X509Certificate rootCert = generateRootCertificate(keyPair, "admin",signatureAlgorithm);
				ks.setCertificateEntry("admin", rootCert);

				save(ks);
				logger.info("...javakeystore created!");
				
			} catch (NoSuchAlgorithmException | CertificateException | IOException | OperatorCreationException
					| KeyStoreException e1) {
				logger.severe("failed to initialize javakeystore: " + e.getMessage());
				e.printStackTrace();
			}

		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			logger.severe("failed to load javakeystore: " + e.getMessage());
			e.printStackTrace();
		}
		return ks;
	}

	public void save(KeyStore ks) {
		try (FileOutputStream fos = new FileOutputStream(keystorepath)) {
			ks.store(fos, keystorepassword.toCharArray());
			logger.info("...keystore saved sucessful to: " + keystorepath);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.severe("failed to store javakeystore: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public KeyPair loadKeyPair(String alias, String password, KeyStore ks)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		Key key = ks.getKey(alias, password.toCharArray());
		if (key instanceof PrivateKey) {
			// Get certificate of public key
			Certificate cert = ks.getCertificate(alias);

			// Get public key
			PublicKey publicKey = cert.getPublicKey();

			// Return a key pair
			return new KeyPair(publicKey, (PrivateKey) key);
		}

		return null;
	}

	/**
	 * Generates a new keyPair based on a given algorithm and a bit length
	 * 
	 * @return
	 */
	public KeyPair createKeyPair(String algorithmIdentifier, int keysize) {
		logger.info("...creating new " + algorithmIdentifier + " keypair...");
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithmIdentifier,
					BouncyCastleProvider.PROVIDER_NAME);
			keyPairGenerator.initialize(keysize);
			return keyPairGenerator.generateKeyPair();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.severe("Failed to generate keypair: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method generates a self signed root certificate. The root certificate
	 * can be used to generate signed X509Certificates.
	 * <p>
	 * The method returns the root certificate.
	 * 
	 * 
	 * @param rootKeyPair - key pair used to generated the certificate
	 * @param cn          - common name of the root certificate
	 * @return a signed X509Certificate
	 * 
	 * @throws OperatorCreationException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws Exception
	 */
	public X509Certificate generateRootCertificate(KeyPair rootKeyPair, String cn, String signatureAlgorithm) throws OperatorCreationException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

		// Setup start date to yesterday and end date for 1 year validity
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		Date startDate = calendar.getTime();

		calendar.add(Calendar.YEAR, 1);
		Date endDate = calendar.getTime();

		BigInteger rootSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));

		// Issued By and Issued To same for root certificate
		X500Name rootCertIssuer = new X500Name("CN=" + cn);
		X500Name rootCertSubject = rootCertIssuer;
		ContentSigner rootCertContentSigner = new JcaContentSignerBuilder(signatureAlgorithm)
				.setProvider(BouncyCastleProvider.PROVIDER_NAME).build(rootKeyPair.getPrivate());
		X509v3CertificateBuilder rootCertBuilder = new JcaX509v3CertificateBuilder(rootCertIssuer, rootSerialNum,
				startDate, endDate, rootCertSubject, rootKeyPair.getPublic());

		// Add Extensions
		// A BasicConstraint to mark root certificate as CA certificate
		JcaX509ExtensionUtils rootCertExtUtils = new JcaX509ExtensionUtils();
		rootCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
		rootCertBuilder.addExtension(Extension.subjectKeyIdentifier, false,
				rootCertExtUtils.createSubjectKeyIdentifier(rootKeyPair.getPublic()));

		// Create a cert holder and export to X509Certificate
		X509CertificateHolder rootCertHolder = rootCertBuilder.build(rootCertContentSigner);
		X509Certificate rootCert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.getCertificate(rootCertHolder);

		// return the certificate
		return rootCert;
	}
}
