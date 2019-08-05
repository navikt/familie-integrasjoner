package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class PersonopplysningerTestConfig {

    private static final String AKTØR_ID = "1000011111111";
    private static final LocalDate TOM = LocalDate.now();
    private static final LocalDate FOM = TOM.minusYears(5);
    private static final Landkoder NORGE = new Landkoder().withValue("NOR");
    private static final Bostedsadresse NORSK_ADRESSE = new Bostedsadresse()
            .withStrukturertAdresse(new Gateadresse()
                    .withGatenavn("Sannergata")
                    .withHusnummer(2)
                    .withPoststed(new Postnummer().withValue("0560"))
                    .withLandkode(NORGE));

    @Bean
    @Profile("mock-personopplysninger")
    @Primary
    public PersonConsumer personConsumerMock() throws HentPersonhistorikkSikkerhetsbegrensning, HentPersonhistorikkPersonIkkeFunnet {
        PersonConsumer personConsumer = mock(PersonConsumer.class);
        when(personConsumer.hentPersonhistorikkResponse(any())).thenReturn(hentPersonHistorikkResponse());
        return personConsumer;
    }

    private HentPersonhistorikkResponse hentPersonHistorikkResponse() {
        HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        response.setAktoer(new AktoerId().withAktoerId(AKTØR_ID));
        response
                .withStatsborgerskapListe(hentStatsborgerskap())
                .withBostedsadressePeriodeListe(hentBostedsadresse());

        return response;
    }

    private Collection<StatsborgerskapPeriode> hentStatsborgerskap() {
        StatsborgerskapPeriode statsborgerskapPeriode = new StatsborgerskapPeriode();
        statsborgerskapPeriode
                .withStatsborgerskap(new Statsborgerskap().withLand(NORGE))
                .withPeriode(new Periode()
                        .withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                        .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(statsborgerskapPeriode);
    }

    private Collection<BostedsadressePeriode> hentBostedsadresse() {
        BostedsadressePeriode bostedsadressePeriode = new BostedsadressePeriode();
        bostedsadressePeriode
                .withBostedsadresse(NORSK_ADRESSE)
                .withPeriode(new Periode()
                        .withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                        .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(bostedsadressePeriode);
    }
}
