/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author salum
 */
@Controller
public class JsonController {

    @Value("${emreg.url}")
    private String emregUrl;

    @Autowired
    private HttpServletRequest context;

    @RequestMapping("/smp/api/smp")//TODO Rename routes
    @ResponseBody
    public List<NCPResult> smpncps() throws Exception {
        return this.ncps();
    }

    @RequestMapping("/api/smp") //TODO Rename routes
    @ResponseBody
    public List<NCPResult> ncps() throws Exception {
        System.out.println("SMP here we go again");

        
        
        List<NCPResult> results;
        results = (List<NCPResult>) context.getSession().getAttribute("ncps");
        if (results == null) {
            results = FiSmpApplication.getNCPs(emregUrl);
            context.getSession().setAttribute("ncps", results);
        }
        return results;
    }
    @RequestMapping("/smp/api/emreg") 
    @ResponseBody
    public String smpemreg() throws URISyntaxException{
        return emreg();
    }
    @RequestMapping("/api/emreg") 
    @ResponseBody
    public String emreg() throws URISyntaxException{
        String emreg  = (String) context.getSession().getAttribute("emreg");
        if (emreg == null){
            RestTemplate template = new RestTemplate();
            emreg = template.getForObject(new URI(emregUrl), String.class);
            context.getSession().setAttribute("emreg", emreg);
        }
        return emreg;
    }
    
    private void printAttributes(HttpServletRequest request) {
        if (request != null) {
            //System.out.println("udi: " + request.getAttribute("uid").toString());

            final Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                final String name = attributeNames.nextElement();
                System.out.println(name + ": " + request.getAttribute(name).toString());
            }
        }
    }

}
