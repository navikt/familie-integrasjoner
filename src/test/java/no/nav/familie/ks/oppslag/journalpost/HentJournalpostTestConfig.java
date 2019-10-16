package no.nav.familie.ks.oppslag.journalpost;

import no.nav.familie.ks.oppslag.journalpost.internal.InnsynJournalConsumer;
import no.nav.familie.ks.oppslag.journalpost.internal.Journalpost;
import no.nav.familie.ks.oppslag.journalpost.internal.SafKlient;
import no.nav.familie.ks.oppslag.journalpost.internal.Sak;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class HentJournalpostTestConfig {

    public static final String NOT_FOUND_CALLID = "NotFoundCallid";
    public static final String GENERISK_ERROR_CALLID = "GeneriskErrorCallid";

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
        return klient;
    }


    @Bean
    @Profile("mock-innsyn")
    @Primary
    public InnsynJournalConsumer InnsynJournalConsumerMock() {
        InnsynJournalConsumer consumer = mock(InnsynJournalConsumer.class);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        when(consumer.hentJournalpost(stringCaptor.capture())).thenAnswer(invocation -> {
            String identArg = invocation.getArgument(0);

            String callId = stringCaptor.getValue();
            if (NOT_FOUND_CALLID.equals(callId)) {
                return null;
            } else if (GENERISK_ERROR_CALLID.equals(callId)) {
                throw new RuntimeException("Feil kastet fra mock");
            } else {
                IdentifiserJournalpostResponse journalpostResponse = new IdentifiserJournalpostResponse();
                journalpostResponse.setJournalpostId("12345678");
                return journalpostResponse;
            }


        });
        return consumer;
    }

}
