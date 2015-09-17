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

    @Value("${emrex.emreg_url}")
    private String emregUrl;

    @Autowired
    private HttpServletRequest context;

    @RequestMapping("/smp/api/smp")
    @ResponseBody
    public List<NCPResult> shome(HttpServletRequest request) throws Exception {
        return this.home(request);
    }

    @RequestMapping("/api/smp")
    @ResponseBody
    public List<NCPResult> home(HttpServletRequest request) throws Exception {
        System.out.println("SMP here we go again");

        printAttributes(request);

        final JSONObject json = getNCPs();

        Object NCPS = json.get("ncps");
        List<Map> ncp_list = (List<Map>) NCPS;
        List<NCPResult> results = ncp_list.stream().map(ncp -> new NCPResult(
                (String) ncp.get("countryCode"),
                (String) ncp.get("acronym"),
                (String) ncp.get("url"),
                (String) ncp.get("pubKey")
        )).collect(Collectors.toList());
        //context.getSession().setAttribute("ncps", results);
        return results;
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

    private JSONObject getNCPs() throws ParseException, URISyntaxException {
        RestTemplate template = new RestTemplate();
        String result = template.getForObject(new URI(emregUrl), String.class);

        System.out.println("Result: " + result);

        return (JSONObject) new JSONParser().parse(result);
    }

}
