package no.nav.familie.ks.oppslag.oppgave.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.http.sts.StsRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OppgaveConfig {

    @Bean
    public OppgaveClient oppgaveClient(@Value("${OPPGAVE_URL}") String url,
                                 @Autowired RestTemplate restTemplate,
                                 @Autowired StsRestClient stsRestClient,
                                 @Autowired ObjectMapper objectMapper) {
        return new OppgaveClient(url, restTemplate, stsRestClient, objectMapper);
    }
}
