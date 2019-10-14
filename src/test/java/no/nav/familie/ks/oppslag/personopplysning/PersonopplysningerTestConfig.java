package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
    public PersonConsumer personConsumerMock() throws HentPersonhistorikkSikkerhetsbegrensning, HentPersonhistorikkPersonIkkeFunnet, HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
        PersonConsumer personConsumer = mock(PersonConsumer.class);
        when(personConsumer.hentPersonhistorikkResponse(any())).thenReturn(hentPersonHistorikkResponse());
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse());
        return personConsumer;
    }

    private HentPersonhistorikkResponse hentPersonHistorikkResponse() {
        HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        response.setAktoer(new AktoerId().withAktoerId(AKTØR_ID));
        response
                .withStatsborgerskapListe(hentStatsborgerskap())
                .withPersonstatusListe(hentPersonstatus())
                .withMidlertidigAdressePeriodeListe(hentMidlertidigAdresse())
                .withBostedsadressePeriodeListe(hentBostedsadresse());

        return response;
    }

    private HentPersonResponse hentPersonResponse() {
        HentPersonResponse response = new HentPersonResponse();
        return response.withPerson(hentPerson());
    }

    private Person hentPerson() {
        Bruker person = new Bruker();
        person
                .withBostedsadresse(NORSK_ADRESSE)
                .withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withKodeRef("MANN")))
                .withSivilstand(new Sivilstand().withSivilstand(new Sivilstander().withValue("GIFT")))
                .withPersonstatus(new Personstatus().withPersonstatus(new Personstatuser().withValue("BOSA")))
                .withPersonnavn(new Personnavn().withSammensattNavn("TEST TESTESEN"))
                .withHarFraRolleI(hentFamilierelasjoner())
                .withGeografiskTilknytning(new Bydel().withGeografiskTilknytning("0315"))
                .withGjeldendePostadressetype(new Postadressetyper().withValue("BOSTEDSADRESSE"))
                .withStatsborgerskap(new Statsborgerskap().withLand(NORGE))
                .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                .withAktoer(new AktoerId().withAktoerId(AKTØR_ID));

        return person;
    }

    private Collection<Familierelasjon> hentFamilierelasjoner() {
        Familierelasjon giftMed = new Familierelasjon();
        giftMed
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("EKTE"))
                .withTilPerson(new Person()
                        .withAktoer(new AktoerId().withAktoerId("1000011111112"))
                        .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                        .withPersonnavn(new Personnavn().withSammensattNavn("EKTEMANN TESTESEN")));

        Familierelasjon barnet = new Familierelasjon();
        barnet
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("BARN"))
                .withTilPerson(new Person()
                        .withAktoer(new AktoerId().withAktoerId("1000011111113"))
                        .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                        .withPersonnavn(new Personnavn().withSammensattNavn("BARN TESTESEN")));

        return Arrays.asList(giftMed, barnet);
    }

    private Foedselsdato hentFoedselsdato(String dato) {
        try {
            return new Foedselsdato().withFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendar(dato));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Collection<PersonstatusPeriode> hentPersonstatus() {
        PersonstatusPeriode personstatusPeriode = new PersonstatusPeriode();
        personstatusPeriode
                .withPersonstatus(new Personstatuser().withValue("BOSA"))
                .withPeriode(new Periode()
                        .withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                        .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(personstatusPeriode);

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

    private Collection<MidlertidigPostadresse> hentMidlertidigAdresse() {
        MidlertidigPostadresseUtland midlertidigPostadresseUtland = new MidlertidigPostadresseUtland();
        midlertidigPostadresseUtland
                .withUstrukturertAdresse(new UstrukturertAdresse()
                        .withAdresselinje1("TEST 1")
                        .withAdresselinje2("TEST 2")
                        .withAdresselinje3("TEST 3")
                        .withLandkode(new Landkoder().withValue("SWE")))
                .withPostleveringsPeriode(new Gyldighetsperiode()
                        .withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                        .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(midlertidigPostadresseUtland);
    }
}
