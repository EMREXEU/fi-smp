package fi.csc.emrex.smp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@Controller
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class
})
public class FiSmpApplication {

    @Value("${emrex.emreg_url}")
    private String emregUrl;

    @Autowired
    private HttpServletRequest context;

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

    @RequestMapping(value="/elmo", method= RequestMethod.POST)
    @ResponseBody
    public FileSystemResource elmo(@ModelAttribute ElmoData request, HttpServletResponse response, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie) throws Exception {

        String sessionId = request.getSessionId();
        String elmo = request.getElmo();

        final String decodedXml = new String(Base64.getDecoder().decode(elmo));

        System.out.println("elmo: " + decodedXml);
        System.out.println("providedSessionId: " + sessionId);

        verifySessionId(sessionId, sessionIdCookie);

        new PdfGen().generatePdf(decodedXml, "/tmp/elmo.pdf");
//        new PdfConverter().writeTextFile(elmo);

        response.setHeader("Content-disposition", "attachment;filename=elmo.pdf");
        response.setContentType("application/pdf");

        return new FileSystemResource("/tmp/elmo.pdf");
    }

    private void verifySessionId(String providedSessionId, String expectedSessionId) {

        System.out.println("expectedSessionId: " + expectedSessionId);

        if (!providedSessionId.equals(expectedSessionId)) {
            throw new RuntimeException("providedSessionId does not match");
        }
    }

    @RequestMapping(value="/toNCP", method= RequestMethod.POST)
    public String toNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletResponse response) throws Exception {

        System.out.println("toNCP");

        response.addCookie(new Cookie("elmoSessionId", context.getSession().getId()));

        model.addAttribute("url", choice.getUrl());
        model.addAttribute("sessionId", context.getSession().getId());
        model.addAttribute("returnUrl", "http://localhost:9002/elmo");

        return "toNCP";
    }

    public static void main(String[] args) {
        SpringApplication.run(FiSmpApplication.class, args);
    }
}
