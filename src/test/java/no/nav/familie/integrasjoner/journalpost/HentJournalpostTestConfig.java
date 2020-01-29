package no.nav.familie.integrasjoner.journalpost;

import no.nav.familie.integrasjoner.journalpost.internal.Journalpost;
import no.nav.familie.integrasjoner.journalpost.internal.SafKlient;
import no.nav.familie.integrasjoner.journalpost.internal.Sak;
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
    public SafKlient safKlientMock() {
        SafKlient klient = mock(SafKlient.class);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        when(klient.hentJournalpost(stringCaptor.capture())).thenAnswer(invocation -> {
            String identArg = invocation.getArgument(0);

            return new Journalpost(stringCaptor.getValue(), new Sak("1111" + stringCaptor.getValue(), "GSAK"));
        });

        doNothing().when(klient).ping();

        return klient;
    }

}
