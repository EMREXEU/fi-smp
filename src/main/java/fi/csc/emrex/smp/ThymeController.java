/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import fi.csc.emrex.smp.model.Person;
import fi.csc.emrex.smp.model.VerificationReply;
import fi.csc.emrex.smp.model.VerifiedReport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author salum
 */
@Controller
public class ThymeController {

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

    @RequestMapping(value = "/smp/onReturn", method = RequestMethod.POST)
    public String smponReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP, HttpServletRequest httpRequest) throws Exception {
        return this.onReturnelmo(request, model, sessionIdCookie, chosenNCP, httpRequest);
    }

    @RequestMapping(value = "/onReturn", method = RequestMethod.POST)
    public String onReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP, HttpServletRequest httpRequest) throws Exception {
        String sessionId = request.getSessionId();
        String elmo = request.getElmo();
        Person person = new Person();
        person.setFirstName(httpRequest.getHeader("shib-cn"));
        person.setLastName(httpRequest.getHeader("shib-sn"));
        person.setGender(httpRequest.getHeader("shib-schacGender"));
        person.setBirthDate(httpRequest.getHeader("shib-schacDateOfBirth"), "YYYYMMDD");

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
        //System.out.println(decodedXml);
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        DocumentBuilder builder;
        if (person != null) {
            List<VerifiedReport> results = new ArrayList<>();
            try {
                VerifiedReport vr = new VerifiedReport();
                builder = factory.newDocumentBuilder();
                StringReader sr = new StringReader(decodedXml);
                InputSource s = new InputSource(sr);

                //Load and Parse the XML document
                //document contains the complete XML as a Tree.
                document = builder.parse(s);
                NodeList reports = document.getElementsByTagName("report");
                for (int i = 0; i < reports.getLength(); i++) {

                    Element report = (Element) reports.item(i);
                    vr.setReport(nodeToString(report));
                    Person elmoPerson = getUserFromElmoReport(report);
                    //Person shibPerson = (Person) context.getSession().getAttribute("shibPerson");

                    if (elmoPerson != null) {
                        VerificationReply verification = person.verifiy(elmoPerson);
                        System.out.println("VerScore: " + verification.getScore());

                        vr.setVerification(verification);

                    } else {
                        vr.addMessage("Elmo learner missing");
                        //todo fix this
                    }
                    results.add(vr);
                }
                context.getSession().setAttribute("reports", results);
                model.addAttribute("reports", results);

            } catch (ParserConfigurationException | IOException | SAXException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(ThymeController.class.getName()).log(Level.SEVERE, null, ex);
                model.addAttribute("error", ex.getMessage());
                return "error";
            }
        } else {

            model.addAttribute("error", "<p>HAKA login missing</p>");
            return "error";
        }
        return "review";
    }

    /**
     * @Deprecated @RequestMapping(value = "/smp/review", method =
     * RequestMethod.POST) public String smpRewiew(@ModelAttribute User user,
     * Model model) { return this.rewiew(user, model); }
     *
     * @Deprecated
     * @RequestMapping(value = "/review", method = RequestMethod.POST) public
     * String rewiew(@ModelAttribute User user, Model model) {
     *
     * String elmoString = (String)
     * context.getSession().getAttribute("elmoxmlstring");
     * model.addAttribute("elmoXml", elmoString);
     * System.out.println(elmoString); Person elmoPerson =
     * getUserFromElmo(elmoString); Person shibPerson = (Person)
     * context.getSession().getAttribute("shibPerson"); VerificationReply
     * verification = shibPerson.verifiy(elmoPerson);
     * System.out.println("VerScore: " + verification.getScore());
     * model.addAttribute("verification", verification); return "review"; }
     */
    private Person getUserFromElmoReport(Element report) {

        Element learner = getOneNode(report, "learner");
        if (learner != null) {
            System.out.println("learner found");
            Person elmoPerson = new Person();
            elmoPerson.setFirstName(getOneNode(learner, "givenNames").getTextContent());
            elmoPerson.setLastName(getOneNode(learner, "familyName").getTextContent());
            Element bday = getOneNode(learner, "bday");
            if (bday != null) {
                elmoPerson.setBirthDate(bday.getTextContent(), bday.getAttribute("dtf"));
            }
            Element gender = getOneNode(learner, "gender");
            if (gender != null) {

                elmoPerson.setGender(gender.getTextContent());
            }
            return elmoPerson;

        } else {
            System.out.println("no learner found");
            return null;
        }

    }


    /*
     private Person getPersonFromElmo(String xml) {
     xml = xml.replaceAll("[\\n\\r]", "");
     DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
     docFactory.setNamespaceAware(false);
     DocumentBuilder docBuilder = null;
     Document doc = null;
     try {
     docBuilder = docFactory.newDocumentBuilder();
     doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
     } catch (Exception e) {
     System.out.println("Failed to parse XML"+ e.getMessage());
     throw new IllegalArgumentException("Failed to parse XML", e);
     }

     NodeList list = doc.getElementsByTagName("report");
     if (list.getLength() == 0) {
     throw new IllegalArgumentException("Failed to get report from XML.");
     }
     Node report = list.item(0);

     Person p = new Person();
     p.setBirthDate(getValueForTag(report, "learner/bday"));
     p.setFamilyName(getValueForTag(report, "learner/familyName"));
     p.setGivenNames(getValueForTag(report, "learner/givenNames"));
     p.setGender("-"); // TODO: We need to expand ELMO to include Gender

     return p;
     }
     */
    private Element getOneNode(Element node, String name) {
        NodeList list = node.getElementsByTagName(name);
        if (list.getLength() == 1) {
            System.out.println("found " + name);
            return (Element) list.item(0);
        } else {
            System.out.println("no " + name + "found");
            return null;
        }
    }

    private String getValueForTag(Node node, String exp) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            return xpath.evaluate(exp, node);
        } catch (Exception e) {
            System.out.println("XPATH error" + e);
            return null;
        }
    }

    private String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }
}
