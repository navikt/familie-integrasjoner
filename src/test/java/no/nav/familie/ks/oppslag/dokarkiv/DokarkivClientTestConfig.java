package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.OpprettJournalpostResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Configuration
public class DokarkivClientTestConfig {

    @Bean
    @Profile("mock-dokarkiv")
    @Primary
    public DokarkivClient dokarkivMockClient() throws Exception {
        DokarkivClient klient = mock(DokarkivClient.class);

        OpprettJournalpostResponse response = new OpprettJournalpostResponse();
        response.setJournalpostId(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        response.setJournalpostferdigstilt(false);

        when(klient.lagJournalpost(any(),anyBoolean(),anyString())).thenReturn(response);
        doNothing().when(klient).ping();
        return klient;
    }

}
