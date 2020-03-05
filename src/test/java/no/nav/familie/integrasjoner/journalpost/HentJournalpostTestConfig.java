package no.nav.familie.integrasjoner.journalpost;

import no.nav.familie.integrasjoner.client.rest.SafRestClient;
import no.nav.familie.integrasjoner.journalpost.domene.Journalpost;
import no.nav.familie.integrasjoner.journalpost.domene.Journalposttype;
import no.nav.familie.integrasjoner.journalpost.domene.Journalstatus;
import no.nav.familie.integrasjoner.journalpost.domene.Sak;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.*;

@Configuration
public class HentJournalpostTestConfig {

    @Bean
    @Profile("mock-saf")
    @Primary
    public SafRestClient safRestClientMock() {
        SafRestClient klient = mock(SafRestClient.class);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        when(klient.hentJournalpost(stringCaptor.capture())).thenAnswer(invocation -> {
            String identArg = invocation.getArgument(0);

            return new Journalpost(
                    stringCaptor.getValue(),
                    Journalposttype.I,
                    Journalstatus.JOURNALFOERT,
                    "BAR",
                    null,
                    new Sak("1111" + stringCaptor.getValue(), "GSAK", null, null),
                    "9999",
                    "EIA",
                    null);
        });

        doNothing().when(klient).ping();

        return klient;
    }

}
