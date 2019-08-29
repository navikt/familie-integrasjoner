package no.nav.familie.ks.oppslag;

import com.nimbusds.jwt.SignedJWT;
import no.nav.security.oidc.api.Unprotected;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;

@RestController
@RequestMapping("/local")
@Profile("dev")
public class STSTestController {

    @Unprotected
    @GetMapping("/sts")
    public AccessTokenResponseTest addCookie(@RequestParam(value = "subject", defaultValue = "srv-user") String subject,
                                             @RequestParam(value = "expiry", required = false) String expiry) {
        long expiryTime = expiry != null ? Long.parseLong(expiry) : JwtTokenGenerator.EXPIRY;
        SignedJWT token = JwtTokenGenerator.createSignedJWT(subject, expiryTime);


        return new AccessTokenResponseTest(token.serialize(), "id_token", Instant.now().plusSeconds( 60 * 60 ).toEpochMilli());
    }
}
