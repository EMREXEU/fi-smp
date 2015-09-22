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

    @Autowired
    private HttpServletRequest context;

    @Value("${emrex.emreg_url}")
    private String emregUrl;
    
        @Value("${return_url}")
    private String returnUrl;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String smp() throws Exception {
        return "smp";
    }
    @RequestMapping(value = "/smp/", method = RequestMethod.GET)
    public String smpsmp() throws Exception {
        return "smp";
    }
    @RequestMapping(value = "/smp/toNCP", method = RequestMethod.POST)
    public String smptoNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletResponse response) throws Exception {
        return toNCP(choice, model, response);
    }
    @RequestMapping(value = "/toNCP", method = RequestMethod.POST)
    public String toNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletResponse response) throws Exception {

        System.out.println("toNCP");
        List<NCPResult> ncps = (List<NCPResult>) getNCPs();// context.getAttribute("ncps");

        response.addCookie(new Cookie("elmoSessionId", context.getSession().getId()));

        model.addAttribute("url", choice.getUrl());
        model.addAttribute("sessionId", context.getSession().getId());
        //TODO Configure this to be dependent on the environem
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while(networkInterfaces.hasMoreElements()){
            NetworkInterface nextElement = networkInterfaces.nextElement();
            System.out.println(nextElement.getDisplayName());
            Enumeration<InetAddress> inetAddresses = nextElement.getInetAddresses();
            while(inetAddresses.hasMoreElements()){
                InetAddress address = inetAddresses.nextElement();
                System.out.println("  "+ address.getHostName());
                System.out.println("  "+ address.getCanonicalHostName());
            }
        }
      

        model.addAttribute("returnUrl", returnUrl);
        response.addCookie(new Cookie("chosenNCP", choice.getUrl()));

        return "toNCP";
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

        String ncpPubKey = null;
//        String returnUrl = (String) context.getAttribute("chosenNCP");
        ncpPubKey = this.getPubKeyByReturnUrl(chosenNCP);

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
    public String smpRewiew(@ModelAttribute User user, Model model){
        return this.rewiew(user, model);
    }
    @RequestMapping(value = "/review", method = RequestMethod.POST)
    public String rewiew(@ModelAttribute User user, Model model){
        
        String elmoString  = (String) context.getSession().getAttribute("elmoxmlstring");
        model.addAttribute("elmoXml", elmoString);
        String name = getUserFromElmo(elmoString);
        if (! user.getName().equals(name) ){
                model.addAttribute("error", "<p>Username deosn't not match elmo</p>");
        }
        
        return "review";
    }

    private String getPubKeyByReturnUrl(String returnUrl) throws Exception {
        String pubKey = null;
        System.out.println("pubkey by url: "+returnUrl);
        if ("https://emrex01.csc.fi/ncp/".equals(returnUrl)){
            //FIXME AS soon as proper configuration in emreg !!!
            returnUrl="http://localhost:9001/norex";
        }
        List<NCPResult> ncps = this.getNCPs();
        for (NCPResult ncp : ncps) {
            if (ncp.getUrl().equals(returnUrl)) {
                System.out.println("Url mathces: "+ returnUrl);
                return ncp.getCertificate();
            }
        }
        return pubKey;
    }

    private List<NCPResult> getNCPs() throws ParseException, URISyntaxException {
        RestTemplate template = new RestTemplate();
        String result = template.getForObject(new URI(emregUrl), String.class);

        System.out.println("Result: " + result);

        final JSONObject json = (JSONObject) new JSONParser().parse(result);

        Object NCPS = json.get("ncps");
        List<Map> ncp_list = (List<Map>) NCPS;
        List<NCPResult> results = ncp_list.stream().map(ncp -> new NCPResult(
                (String) ncp.get("countryCode"),
                (String) ncp.get("acronym"),
                (String) ncp.get("url"),
                (String) ncp.get("pubKey")
        )).collect(Collectors.toList());
        context.getSession().setAttribute("ncps", results);
        return results;
    }

    private String getUserFromElmo(String elmoString) {
        return "";
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
