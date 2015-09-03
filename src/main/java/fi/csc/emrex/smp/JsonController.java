/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    @RequestMapping("/smp")
    @ResponseBody
    public List<NCPResult> home() throws Exception {
        System.out.println("SMP here we go again");

        final JSONObject json = getNCPs();

        Object NCPS = json.get("ncps");
        List<Map> ncp_list = (List<Map>) NCPS;
        List<NCPResult> results = ncp_list.stream().map(ncp -> new NCPResult(
                (String) ncp.get("countryCode"),
                (String) ncp.get("acronym"),
                (String) ncp.get("url"))).collect(Collectors.toList());

        return results;
    }
        
    private JSONObject getNCPs() throws ParseException, URISyntaxException {
        RestTemplate template = new RestTemplate();
        String result = template.getForObject(new URI(emregUrl), String.class);

        System.out.println("Result: " + result);

        return (JSONObject)new JSONParser().parse(result);
    }

}
