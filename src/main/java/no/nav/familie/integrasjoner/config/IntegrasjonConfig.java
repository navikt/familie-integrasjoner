package no.nav.familie.integrasjoner.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.sts.StsRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;

@Configuration
public class IntegrasjonConfig {

    @Bean
    @Autowired
    @Profile("!mock-sts")
    public StsRestClient stsRestClient(ObjectMapper objectMapper,
                                       @Value("${STS_URL}") URI stsUrl,
                                       @Value("${CREDENTIAL_USERNAME}") String stsUsername,
                                       @Value("${CREDENTIAL_PASSWORD}") String stsPassword) {

        final var stsFullUrl = URI.create(stsUrl + "?grant_type=client_credentials&scope=openid");

        return new StsRestClient(objectMapper, stsFullUrl, stsUsername, stsPassword);
    }
}
