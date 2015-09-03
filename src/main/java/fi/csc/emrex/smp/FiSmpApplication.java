package fi.csc.emrex.smp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
