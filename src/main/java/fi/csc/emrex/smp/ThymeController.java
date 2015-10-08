/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.smp.model.Person;
import fi.csc.emrex.smp.model.VerificationReply;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author salum
 */
@Controller
public class ThymeController {

    public static final String SHIB_SHIB_IDENTITY_PROVIDER = "shib-Shib-Identity-Provider";
    @Autowired
    private HttpServletRequest context;

    @Value("${emreg.url}")
    private String emregUrl;

    @Value("${smp.return.url}")
    private String returnUrl;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String smp(HttpServletRequest request, Model model) throws Exception {
        return smpsmp(request, model);
    }

    @RequestMapping(value = "/smp/", method = RequestMethod.GET)
    public String smpsmp(HttpServletRequest request, Model model) throws Exception {
        String firstName = request.getHeader("shib-givenName");

        model.addAttribute("name", firstName);
        return "smp";
    }

    private void printAttributes(HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        System.out.println("requestURI: " + requestURI);

        final String requestURL = request.getRequestURL().toString();
        System.out.println("requestURL: " + requestURL);

        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
    }

    @RequestMapping(value = "/smp/toNCP", method = RequestMethod.POST)
    public String smptoNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return toNCP(choice, model, request, response);
    }

    @RequestMapping(value = "/toNCP", method = RequestMethod.POST)
    public String toNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("toNCP");

        response.addCookie(new Cookie("elmoSessionId", context.getSession().getId()));
        response.addCookie(new Cookie("chosenNCP", getPubKeyByReturnUrl(choice.getUrl())));

        model.addAttribute("url", getUrl(choice, request));
        model.addAttribute("sessionId", context.getSession().getId());
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("ncp", getNCPByReturnUrl(choice.getUrl())); //TODO can be null?

        return "toNCP";
    }

    private String getUrl(NCPChoice choice, HttpServletRequest request) {
        final String idp = request.getHeader(SHIB_SHIB_IDENTITY_PROVIDER);
        return idp != null ? choice.getUrl() + "Shibboleth.sso/Login?entityID=" + request.getHeader(SHIB_SHIB_IDENTITY_PROVIDER) : choice.getUrl();
    }

    @RequestMapping(value = "/smp/onReturn", method = RequestMethod.POST)
    public String smponReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP, HttpServletRequest httpRequest) throws Exception {
        return this.onReturnelmo(request, model, sessionIdCookie, chosenNCP, httpRequest);
    }

    @RequestMapping(value = "/onReturn", method = RequestMethod.POST)
    public String onReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP, HttpServletRequest httpRequest) throws Exception {
        String sessionId = request.getSessionId();
        String elmo = request.getElmo();
        Person person = new Person("YYYYMMDD");
        person.setFirstName(httpRequest.getHeader("shib-cn"));
        person.setLastName(httpRequest.getHeader("shib-sn"));
        person.setGender(httpRequest.getHeader("shib-schacGender"));
        person.setBirthDate(httpRequest.getHeader("shib-schacDateOfBirth"));

        context.getSession().setAttribute("shibPerson", person);
        final String decodedXml = new String(Base64.getDecoder().decode(elmo));

        //System.out.println("elmo: " + decodedXml);
        System.out.println("providedSessionId: " + sessionId);

        String ncpPubKey = chosenNCP;

        try {
            FiSmpApplication.verifySessionId(sessionId, sessionIdCookie);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", "<p>Session verification failed</p>");
            return "error";
        }
        try {
            if (!FiSmpApplication.verifyElmoSignature(decodedXml, ncpPubKey)) {
                model.addAttribute("error", "<p>NCP signature check failed</p>");
                return "error";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            model.addAttribute("error", "<p>NCP verification failed</p>");
            return "error";
        }
        context.getSession().setAttribute("elmoxmlstring", decodedXml);
        model.addAttribute("elmoXml", decodedXml);
        Person elmoPerson = getUserFromElmo(decodedXml);
        //Person shibPerson = (Person) context.getSession().getAttribute("shibPerson");
        if (person != null) {
            if (elmoPerson != null) {
                VerificationReply verification = person.verifiy(elmoPerson);
                System.out.println("VerScore: " + verification.getScore());
                model.addAttribute("verification", verification);
                return "review";
            } else {
                model.addAttribute("error", "<p>Elmo learner missing</p>");
                return "review"; //todo fix this
            }
        } else {
            model.addAttribute("error", "<p>HAKA login missing</p>");
            return "error";
        }
    }

    @Deprecated
    @RequestMapping(value = "/smp/review", method = RequestMethod.POST)
    public String smpRewiew(@ModelAttribute User user, Model model) {
        return this.rewiew(user, model);
    }

    @Deprecated
    @RequestMapping(value = "/review", method = RequestMethod.POST)
    public String rewiew(@ModelAttribute User user, Model model) {

        String elmoString = (String) context.getSession().getAttribute("elmoxmlstring");
        model.addAttribute("elmoXml", elmoString);
        Person elmoPerson = getUserFromElmo(elmoString);
        Person shibPerson = (Person) context.getSession().getAttribute("shibPerson");
        VerificationReply verification = shibPerson.verifiy(elmoPerson);
        System.out.println("VerScore: " + verification.getScore());
        model.addAttribute("verification", verification);
        return "review";
    }

    private NCPResult getNCPByReturnUrl(String returnUrl) throws Exception {
        String pubKey = null;
        List<NCPResult> ncps = FiSmpApplication.getNCPs(emregUrl);
        for (NCPResult ncp : ncps) {
            if (ncp.getUrl().equals(returnUrl)) {

                return ncp;
            }
        }
        return null;
    }

    private String getPubKeyByReturnUrl(String returnUrl) throws Exception {
        String pubKey = null;
        System.out.println("pubkey by url: " + returnUrl);
        List<NCPResult> ncps = FiSmpApplication.getNCPs(emregUrl);
        for (NCPResult ncp : ncps) {
            if (ncp.getUrl().equals(returnUrl)) {
                System.out.println("Url mathces: " + returnUrl);
                return ncp.getCertificate();
            }
        }
        return pubKey;
    }

    private Person getUserFromElmo(String elmoString) {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(elmoString);
            InputSource s = new InputSource(sr);

            //Load and Parse the XML document
            //document contains the complete XML as a Tree.
            document = builder.parse(s);

            Element learner = getOneNode(document.getDocumentElement(), "learner");
            if (learner != null) {
                System.out.println("learner found");
                Element bday = getOneNode(learner, "bday");
                Person elmoPerson = new Person(bday.getAttribute("dtf"));
                elmoPerson.setBirthDate(bday.getTextContent());
                elmoPerson.setFirstName(getOneNode(learner, "givenNames").getTextContent());
                elmoPerson.setLastName(getOneNode(learner, "familyName").getTextContent());
                elmoPerson.setGender(getOneNode(learner, "gender").getTextContent());
                return elmoPerson;

            } else {
                   System.out.println("no learner found");
                return null;
            }

        } catch (ParserConfigurationException | IOException | SAXException ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(ThymeController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private Element getOneNode(Element node, String name) {
        NodeList list = node.getElementsByTagName(name);
        if (list.getLength() == 1) {
            return (Element) list.item(0);
        } else {
            return null;
        }
    }

}
