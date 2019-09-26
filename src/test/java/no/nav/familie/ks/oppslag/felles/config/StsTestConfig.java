package no.nav.familie.ks.oppslag.felles.config;

import no.nav.familie.ks.oppslag.felles.rest.StsRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class StsTestConfig {
    @Bean
    @Profile("mock-sts")
    @Primary
    public StsRestClient stsRestClientMock() {
        StsRestClient client = mock(StsRestClient.class);

        when(client.getSystemOIDCToken()).thenReturn("MOCKED-OIDC-TOKEN");
        return client;
    }
}