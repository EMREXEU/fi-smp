/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

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
        String firstName=request.getHeader("shib-givenName");
        String  personId= request.getHeader("shib-uid");
        printAttributes(request);
        model.addAttribute("name",firstName);
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
    public String smponReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP) throws Exception {
        return this.onReturnelmo(request, model, sessionIdCookie, chosenNCP);
    }

    @RequestMapping(value = "/onReturn", method = RequestMethod.POST)
    public String onReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie, @CookieValue(value = "chosenNCP") String chosenNCP) throws Exception {
        String sessionId = request.getSessionId();
        String elmo = request.getElmo();

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

        return "review";
    }

    @RequestMapping(value = "/smp/review", method = RequestMethod.POST)
    public String smpRewiew(@ModelAttribute User user, Model model) {
        return this.rewiew(user, model);
    }

    @RequestMapping(value = "/review", method = RequestMethod.POST)
    public String rewiew(@ModelAttribute User user, Model model) {

        String elmoString = (String) context.getSession().getAttribute("elmoxmlstring");
        model.addAttribute("elmoXml", elmoString);
        String name = getUserFromElmo(elmoString);
        if (!user.getName().equals(name)) {
            model.addAttribute("error", "<p>Username deosn't not match elmo</p>");
        }

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

    private String getUserFromElmo(String elmoString) {
        return "";
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
