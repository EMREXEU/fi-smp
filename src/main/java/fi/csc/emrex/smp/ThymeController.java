/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.csc.emrex.smp;

import java.util.Base64;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author salum
 */
@Controller
public class ThymeController {

    @Autowired
    private HttpServletRequest context;

    @RequestMapping(value = "/toNCP", method = RequestMethod.POST)
    public String toNCP(@ModelAttribute NCPChoice choice, Model model, HttpServletResponse response) throws Exception {

        System.out.println("toNCP");

        response.addCookie(new Cookie("elmoSessionId", context.getSession().getId()));

        model.addAttribute("url", choice.getUrl());
        model.addAttribute("sessionId", context.getSession().getId());
        model.addAttribute("returnUrl", "http://localhost:9002/onReturn");

        return "toNCP";
    }

    @RequestMapping(value = "/onReturn", method = RequestMethod.POST)
    public String onReturnelmo(@ModelAttribute ElmoData request, Model model, @CookieValue(value = "elmoSessionId") String sessionIdCookie) throws Exception{
        String sessionId = request.getSessionId();
        String elmo = request.getElmo();

        final String decodedXml = new String(Base64.getDecoder().decode(elmo));

        System.out.println("elmo: " + decodedXml);
        System.out.println("providedSessionId: " + sessionId);
        model.addAttribute("elmoXml", "<h1>Session verification failed<h1>");
        FiSmpApplication.verifySessionId(sessionId, sessionIdCookie);
        context.getSession().setAttribute("elmoxmlstring", decodedXml);
        model.addAttribute("elmoXml", decodedXml);
        return "onReturn";
    }
}
