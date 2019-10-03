package no.nav.familie.ks.oppslag.medlemskap.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.sts.StsRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedlClientConfig {

    @Bean
    public MedlClient medlClient(@Value("${MEDL2_URL}") String url,
                                 @Value("${CREDENTIAL_USERNAME}") String srvBruker,
                                 @Autowired StsRestClient stsRestClient,
                                 @Autowired ObjectMapper objectMapper) {
        return new MedlClient(url, srvBruker, stsRestClient, objectMapper);
    }
}
