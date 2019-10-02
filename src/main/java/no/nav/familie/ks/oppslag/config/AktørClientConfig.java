package no.nav.familie.ks.oppslag.config;


import no.nav.familie.http.sts.StsRestClient;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AktørClientConfig {

    @Bean
    public AktørregisterClient aktørregisterClient(@Value("${AKTOERID_URL}") String aktørRegisterUrl,
                                                   @Value("${CREDENTIAL_USERNAME}") String consumer,
                                                   @Autowired StsRestClient stsRestClient) {
        return new AktørregisterClient(aktørRegisterUrl, consumer, stsRestClient);
    }
}
