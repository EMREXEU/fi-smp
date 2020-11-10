package fi.csc.emrex.smp;


import fi.csc.emrex.smp.util.PdfGen;
import fi.csc.emrex.smp.util.SignatureVerifier;
import fi.csc.emrex.smp.util.TestUtil;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by marko.hollanti on 07/10/15.
 */
public class PDFGenTests extends TestCase {

  private SignatureVerifier instance;

  public void setUp() throws Exception {
  }

  @Test
  public void testFinElmo() throws Exception {
    generatePdf("Example-elmo-complicated.xml", "/tmp/elmo-complicated.pdf");
    generatePdf("Example-elmo-Finland.xml", "/tmp/elmo-finland.pdf");
    generatePdf("Example-elmo-Norway.xml", "/tmp/elmo-norway.pdf");
  }

  private void generatePdf(String filename, String uri) throws Exception {
    final String decodedXml = TestUtil.getFileContent(filename);
    new PdfGen().generatePdf(decodedXml, uri);
  }
}


