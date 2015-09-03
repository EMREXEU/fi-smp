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

@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class
})
public class FiSmpApplication {


    public static void verifySessionId(String providedSessionId, String expectedSessionId) {

        System.out.println("expectedSessionId: " + expectedSessionId);

        if (!providedSessionId.equals(expectedSessionId)) {
            throw new RuntimeException("providedSessionId does not match");
        }
    }



    public static void main(String[] args) {
        SpringApplication.run(FiSmpApplication.class, args);
    }
}
