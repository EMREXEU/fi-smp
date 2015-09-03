/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author salum
 */
@Controller
public class PDFController {
        @RequestMapping(value="/elmo", method= RequestMethod.POST)
    @ResponseBody
    public FileSystemResource elmo(@ModelAttribute ElmoData request, HttpServletResponse response, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie) throws Exception {

        String sessionId = request.getSessionId();
        String elmo = request.getElmo();

        final String decodedXml = new String(Base64.getDecoder().decode(elmo));

        System.out.println("elmo: " + decodedXml);
        System.out.println("providedSessionId: " + sessionId);

        FiSmpApplication.verifySessionId(sessionId, sessionIdCookie);

        new PdfGen().generatePdf(decodedXml, "/tmp/elmo.pdf");
//        new PdfConverter().writeTextFile(elmo);

        response.setHeader("Content-disposition", "attachment;filename=elmo.pdf");
        response.setContentType("application/pdf");

        return new FileSystemResource("/tmp/elmo.pdf");
    }

}
