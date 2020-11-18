/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp.controller;

import fi.csc.emrex.smp.FiSmpApplication;
import fi.csc.emrex.smp.dto.NCPChoice;
import fi.csc.emrex.smp.dto.NCPResult;
import fi.csc.emrex.smp.dto.SessionData;
import fi.csc.emrex.smp.model.VerifiedReport;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;


/**
 * @author salum
 */
@Controller
public class JsonController {

  @Value("${emreg.url}")
  private String emregUrl;

  @Value("${smp.return.url}")
  private String returnUrl;

  @Autowired
  private HttpServletRequest context;

  @Deprecated
  @RequestMapping("/smp/api/smp")
  @ResponseBody
  public List<NCPResult> smpncps() throws Exception {
    return this.fetchNcpsFromEmregJson();
  }

  @RequestMapping(value = "/smp/api/sessiondata", method = RequestMethod.POST)
  @ResponseBody
  public SessionData smpSessionData(@RequestBody NCPChoice choice, HttpServletRequest request)
      throws Exception {
    return this.sessionData(choice, request);
  }

  @RequestMapping(value = "/api/sessiondata", method = RequestMethod.POST)
  @ResponseBody
  public SessionData sessionData(@RequestBody NCPChoice choice, HttpServletRequest request)
      throws Exception {
    SessionData result = new SessionData();
    result.setElmoSessionId(context.getSession().getId());
    result.setNcpPublicKey(FiSmpApplication.getPubKeyByReturnUrl(choice.getUrl(), emregUrl));
    result.setUrl(FiSmpApplication.getUrl(choice, request));
    result.setSessionId(context.getSession().getId());
    result.setReturnUrl(returnUrl);
    return result;
  }

  @RequestMapping("/api/smp")
  @ResponseBody
  public List<NCPResult> fetchNcpsFromEmregJson() throws Exception {
    List<NCPResult> results;
    results = (List<NCPResult>) context.getSession().getAttribute("ncps");
    if (results == null) {
      results = FiSmpApplication.getNCPs(emregUrl);
      context.getSession().setAttribute("ncps", results);
    }
    return results;
  }

  @Deprecated
  @RequestMapping("/smp/api/emreg")
  @ResponseBody
  public String smpemreg() throws URISyntaxException {
    return fetchNcpsFromEmreg();
  }

  @RequestMapping("/api/emreg")
  @ResponseBody
  public String fetchNcpsFromEmreg() throws URISyntaxException {
    String ncpsInJson = (String) context.getSession().getAttribute("emreg");
    if (ncpsInJson == null) {
      RestTemplate template = new RestTemplate();
      ncpsInJson = template.getForObject(new URI(emregUrl), String.class);
      context.getSession().setAttribute("emreg", ncpsInJson);
    }
    return ncpsInJson;
  }

  @Deprecated
  @RequestMapping("/smp/api/reports")
  @ResponseBody
  public List<VerifiedReport> smpreports() {
    return reports();
  }

  @RequestMapping("/api/reports")
  @ResponseBody
  public List<VerifiedReport> reports() {
    return (List<VerifiedReport>) this.context.getSession().getAttribute("reports");
  }

  @Deprecated
  private void printAttributes(HttpServletRequest request) {
    if (request != null) {

      final Enumeration<String> attributeNames = request.getAttributeNames();
      while (attributeNames.hasMoreElements()) {
        final String name = attributeNames.nextElement();
        System.out.println(name + ": " + request.getAttribute(name).toString());
      }
    }
  }

}