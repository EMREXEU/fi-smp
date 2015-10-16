package fi.csc.emrex.smp;

import fi.csc.emrex.smp.util.TestUtil;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

/**
 * Created by marko.hollanti on 07/10/15.
 */
@SpringApplicationConfiguration
public class SignatureVerifierTest extends TestCase {

    private SignatureVerifier instance;

    @Before
    public void setUp() throws Exception {
        instance = new SignatureVerifier();
        instance.setCertificatePath("csc-cert.crt");
        instance.setEnvironment("dev");
    }

    @Test
    public void testVerifySignature() throws Exception {

        final String dataOk = TestUtil.getFileContent("elmo_vastaus_base64_gzipped.txt");

        assertTrue(instance.verifySignature(dataOk));

    }
}