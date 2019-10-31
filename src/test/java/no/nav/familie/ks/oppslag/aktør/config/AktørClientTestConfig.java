package no.nav.familie.ks.oppslag.aktør.config;

import no.nav.familie.ks.kontrakter.søknad.testdata.SøknadTestdata;
import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.domene.Ident;
import no.nav.familie.ks.oppslag.aktør.internal.AktørResponse;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

import static org.mockito.Mockito.*;

@Configuration
public class AktørClientTestConfig {

    @Bean
    @Profile("mock-aktor")
    @Primary
    public AktørregisterClient aktørregisterClientMock() throws Exception {
        AktørregisterClient aktørregisterClient = mock(AktørregisterClient.class);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        when(aktørregisterClient.hentAktørId(stringCaptor.capture())).thenAnswer(invocation -> {
            String identArg = invocation.getArgument(0);

            return new AktørResponse()
                    .withAktør(identArg, new Aktør().withIdenter(Collections.singletonList(finnRiktigAktørId(identArg))));
        });

        when(aktørregisterClient.hentPersonIdent(stringCaptor.capture())).thenAnswer(invocation -> {
            String identArg = invocation.getArgument(0);

            return new AktørResponse()
                    .withAktør(identArg, new Aktør().withIdenter(Collections.singletonList(finnRiktigPersonIdent(identArg))));
        });

        doNothing().when(aktørregisterClient).ping();
        return aktørregisterClient;
    }

    private Ident finnRiktigAktørId(String personIdent) {
        switch (personIdent) {
            case SøknadTestdata.morPersonident:
                return new Ident().withIdent(SøknadTestdata.morAktørId).withGjeldende(true);
            case SøknadTestdata.barnPersonident:
                return new Ident().withIdent(SøknadTestdata.barnAktørId).withGjeldende(true);
            case SøknadTestdata.farPersonident:
                return new Ident().withIdent(SøknadTestdata.farAktørId).withGjeldende(true);
            case SøknadTestdata.utenlandskMorPersonident:
                return new Ident().withIdent(SøknadTestdata.utenlandskMorAktørId).withGjeldende(true);
            case SøknadTestdata.utenlandskBarnPersonident:
                return new Ident().withIdent(SøknadTestdata.utenlandskBarnAktørId).withGjeldende(true);
            case SøknadTestdata.utenlandskFarPersonident:
                return new Ident().withIdent(SøknadTestdata.utenlandskFarAktørId).withGjeldende(true);
            default:
                return new Ident().withIdent("1000011111111").withGjeldende(true);
        }
    }

    private Ident finnRiktigPersonIdent(String aktørId) {
        switch (aktørId) {
            case SøknadTestdata.morAktørId:
                return new Ident().withIdent(SøknadTestdata.morPersonident).withGjeldende(true);
            case SøknadTestdata.barnAktørId:
                return new Ident().withIdent(SøknadTestdata.barnPersonident).withGjeldende(true);
            case SøknadTestdata.farAktørId:
                return new Ident().withIdent(SøknadTestdata.farPersonident).withGjeldende(true);
            case SøknadTestdata.utenlandskMorAktørId:
                return new Ident().withIdent(SøknadTestdata.utenlandskMorPersonident).withGjeldende(true);
            case SøknadTestdata.utenlandskBarnAktørId:
                return new Ident().withIdent(SøknadTestdata.utenlandskBarnPersonident).withGjeldende(true);
            case SøknadTestdata.utenlandskFarAktørId:
                return new Ident().withIdent(SøknadTestdata.utenlandskFarPersonident).withGjeldende(true);
            default:
                return new Ident().withIdent("10000111111").withGjeldende(true);
        }
    }
}
