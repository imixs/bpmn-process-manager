package org.imixs.bpmn.manager.keystore;

import java.io.File;
import java.security.KeyStore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test class tests X509CertificateGenerator
 * 
 * @author rsoika
 * @version 1.0
 */
@Ignore
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class X509CertificateGeneratorTest {

    String resourcesPath = null;
    String keyStorePath = null;
    String keystorePassword = "123456";
    KeystoreService keystoreService;
  /**
     * Init resource path to test resources
     * 
     * @throws PluginException
     * @throws ModelException
     */
    @Before
    public void setup() {
        resourcesPath = new File("src/test/resources").getAbsolutePath();
        keyStorePath = resourcesPath + "/keystore/test.jks";
        keystoreService = new KeystoreService();

        
        
    }
    

    /**
     * This test method generates a new password protected root certificate.
     * <p>
     * Note: a password protection is optional but should be used for root
     * certificates!
     */
    @Test
    public void test001GenerateRootCert() {
        try {
            KeyStore keyStore = keystoreService.load();
            
           

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    
}
