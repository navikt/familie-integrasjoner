package no.nav.familie.integrasjoner.personopplysning;

import no.nav.familie.integrasjoner.client.soap.PersonSoapClient;
import no.nav.familie.integrasjoner.felles.ws.DateUtil;
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;

@Configuration
public class PersonopplysningerTestConfig {

    private static final LocalDate TOM = LocalDate.now();
    private static final LocalDate FOM = TOM.minusYears(5);
    private static final LocalDate FOM_BARNET = LocalDate.of(2018, 5, 1);
    private static final Landkoder NORGE = new Landkoder().withValue("NOR");
    private static final Bostedsadresse NORSK_ADRESSE = new Bostedsadresse()
            .withStrukturertAdresse(new Gateadresse()
                                            .withGatenavn("Sannergata")
                                            .withHusnummer(2)
                                            .withPoststed(new Postnummer().withValue("0560"))
                                            .withLandkode(NORGE));
    private static final PersonIdent MOR_PERSON_IDENT =
            new PersonIdent().withIdent(new NorskIdent().withIdent(SøknadTestdata.morPersonident));
    private static final PersonIdent BARN_PERSON_IDENT =
            new PersonIdent().withIdent(new NorskIdent().withIdent(SøknadTestdata.barnPersonident));
    private static final PersonIdent FAR_PERSON_IDENT =
            new PersonIdent().withIdent(new NorskIdent().withIdent(SøknadTestdata.farPersonident));

    @Bean
    @Profile("mock-personopplysninger")
    @Primary
    public PersonSoapClient personConsumerMock() throws
                                               HentPersonhistorikkSikkerhetsbegrensning,
                                               HentPersonhistorikkPersonIkkeFunnet,
                                               HentPersonSikkerhetsbegrensning,
                                               HentPersonPersonIkkeFunnet {
        PersonSoapClient personConsumer = mock(PersonSoapClient.class);
        ArgumentCaptor<HentPersonRequest> personRequestCaptor = ArgumentCaptor.forClass(HentPersonRequest.class);
        ArgumentCaptor<HentPersonhistorikkRequest> historikkRequestCaptor =
                ArgumentCaptor.forClass(HentPersonhistorikkRequest.class);

        when(personConsumer.hentPersonhistorikkResponse(historikkRequestCaptor.capture())).thenAnswer(invocation -> {
            if (historikkRequestCaptor.getValue() == null) {
                return null;
            }

            PersonIdent personIdent = (PersonIdent) historikkRequestCaptor.getValue().getAktoer();
            if (SøknadTestdata.barnPersonident.equals(personIdent.getIdent().getIdent())) {
                return hentPersonhistorikkResponseBarn();
            }
            return hentPersonHistorikkResponse(personIdent.getIdent().getIdent().equals(SøknadTestdata.morPersonident));
        });

        when(personConsumer.hentPersonResponse(personRequestCaptor.capture())).thenAnswer(invocation -> {
            if (personRequestCaptor.getValue() == null) {
                return null;
            }

            PersonIdent personIdent = (PersonIdent) personRequestCaptor.getValue().getAktoer();

            if (SøknadTestdata.morPersonident.equals(personIdent.getIdent().getIdent())) {
                return hentPersonResponseForMor();
            }
            if (SøknadTestdata.barnPersonident.equals(personIdent.getIdent().getIdent())) {
                return hentPersonResponseForBarn();
            }
            return hentPersonResponseForFar();
        });

        doNothing().when(personConsumer).ping();
        return personConsumer;
    }

    private static HentPersonhistorikkResponse hentPersonHistorikkResponse(boolean erMor) {
        HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        response.setAktoer(erMor ? MOR_PERSON_IDENT : FAR_PERSON_IDENT);
        response
                .withStatsborgerskapListe(hentStatsborgerskap(false))
                .withPersonstatusListe(hentPersonstatus(false))
                .withMidlertidigAdressePeriodeListe(hentMidlertidigAdresse())
                .withBostedsadressePeriodeListe(hentBostedsadresse(false));

        return response;
    }

    private static HentPersonhistorikkResponse hentPersonhistorikkResponseBarn() {
        HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        response.setAktoer(BARN_PERSON_IDENT);
        response
                .withStatsborgerskapListe(hentStatsborgerskap(true))
                .withBostedsadressePeriodeListe(hentBostedsadresse(true))
                .withPersonstatusListe(hentPersonstatus(true));

        return response;
    }

    private static HentPersonResponse hentPersonResponseForMor() {
        HentPersonResponse response = new HentPersonResponse();
        return response.withPerson(hentPersoninfoMor());
    }

    private static HentPersonResponse hentPersonResponseForBarn() {
        HentPersonResponse response = new HentPersonResponse();
        return response.withPerson(hentPersoninfoBarn());
    }

    private static HentPersonResponse hentPersonResponseForFar() {
        HentPersonResponse response = new HentPersonResponse();
        return response.withPerson(hentPersoninfoFar());
    }

    private static Person hentPersoninfoMor() {
        Bruker mor = hentStandardPersoninfo();
        mor
                .withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue("K")))
                .withSivilstand(new Sivilstand().withSivilstand(new Sivilstander().withValue("GIFT")))
                .withPersonnavn(new Personnavn().withSammensattNavn("TEST TESTESEN"))
                .withHarFraRolleI(hentFamilierelasjonerMor())
                .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                .withAktoer(MOR_PERSON_IDENT);

        return mor;
    }

    private static Person hentPersoninfoFar() {
        Bruker far = hentStandardPersoninfo();
        far
                .withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue("M")))
                .withSivilstand(new Sivilstand().withSivilstand(new Sivilstander().withValue("GIFT")))
                .withPersonnavn(new Personnavn().withSammensattNavn("EKTEMANN TESTESEN"))
                .withHarFraRolleI(hentFamilierelasjonerFar())
                .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                .withAktoer(FAR_PERSON_IDENT);

        return far;
    }

    private static Person hentPersoninfoBarn() {
        Bruker barn = hentStandardPersoninfo();
        barn.withKjoenn(new Kjoenn().withKjoenn(new Kjoennstyper().withValue("K")))
            .withSivilstand(new Sivilstand().withSivilstand(new Sivilstander().withValue("UGIF")))
            .withPersonnavn(new Personnavn().withSammensattNavn("BARN TESTESEN"))
            .withHarFraRolleI(hentFamilierelasjonerBarn())
            .withFoedselsdato(hentFoedselsdato("2018-05-01"))
            .withAktoer(BARN_PERSON_IDENT);

        return barn;
    }

    private static Bruker hentStandardPersoninfo() {
        Bruker person = new Bruker();
        person.withPersonstatus(new Personstatus().withPersonstatus(new Personstatuser().withValue("BOSA")))
              .withGeografiskTilknytning(new Bydel().withGeografiskTilknytning("0315"))
              .withGjeldendePostadressetype(new Postadressetyper().withValue("BOSTEDSADRESSE"))
              .withStatsborgerskap(new Statsborgerskap().withLand(NORGE))
              .withBostedsadresse(NORSK_ADRESSE);

        return person;
    }

    private static Collection<Familierelasjon> hentFamilierelasjonerMor() {
        Familierelasjon giftMed = new Familierelasjon();
        giftMed
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("EKTE"))
                .withTilPerson(new Person()
                                       .withAktoer(new PersonIdent().withIdent(new NorskIdent()
                                                                                       .withIdent(SøknadTestdata.farPersonident)))
                                       .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("EKTEMANN TESTESEN")));

        Familierelasjon barnet = new Familierelasjon();
        barnet
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("BARN"))
                .withTilPerson(new Person()
                                       .withAktoer(BARN_PERSON_IDENT)
                                       .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("BARN TESTESEN")));

        return Arrays.asList(giftMed, barnet);
    }

    private static Collection<Familierelasjon> hentFamilierelasjonerBarn() {
        Familierelasjon far = new Familierelasjon();
        far
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("FARA"))
                .withTilPerson(new Person()
                                       .withAktoer(new PersonIdent().withIdent(new NorskIdent()
                                                                                       .withIdent(SøknadTestdata.farPersonident)))
                                       .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("EKTEMANN TESTESEN")));

        Familierelasjon mor = new Familierelasjon();
        mor
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("MORA"))
                .withTilPerson(new Person()
                                       .withAktoer(MOR_PERSON_IDENT)
                                       .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("TEST TESTESEN")));

        return Arrays.asList(far, mor);
    }

    private static Collection<Familierelasjon> hentFamilierelasjonerFar() {
        Familierelasjon giftMed = new Familierelasjon();
        giftMed
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("EKTE"))
                .withTilPerson(new Person()
                                       .withAktoer(MOR_PERSON_IDENT)
                                       .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("TEST TESTESEN")));

        Familierelasjon barnet = new Familierelasjon();
        barnet
                .withHarSammeBosted(true)
                .withTilRolle(new Familierelasjoner().withValue("BARN"))
                .withTilPerson(new Person()
                                       .withAktoer(BARN_PERSON_IDENT)
                                       .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                       .withPersonnavn(new Personnavn().withSammensattNavn("BARN TESTESEN")));

        return Arrays.asList(giftMed, barnet);
    }

    private static Foedselsdato hentFoedselsdato(String dato) {
        try {
            return new Foedselsdato().withFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendar(dato));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Collection<PersonstatusPeriode> hentPersonstatus(boolean erBarnet) {
        PersonstatusPeriode personstatusPeriode = new PersonstatusPeriode();
        personstatusPeriode
                .withPersonstatus(new Personstatuser().withValue("BOSA"))
                .withPeriode(new Periode()
                                     .withFom(DateUtil.convertToXMLGregorianCalendar(erBarnet ? FOM_BARNET : FOM))
                                     .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(personstatusPeriode);

    }

    private static Collection<StatsborgerskapPeriode> hentStatsborgerskap(boolean erBarnet) {
        StatsborgerskapPeriode statsborgerskapPeriode = new StatsborgerskapPeriode();
        statsborgerskapPeriode
                .withStatsborgerskap(new Statsborgerskap().withLand(NORGE))
                .withPeriode(new Periode()
                                     .withFom(DateUtil.convertToXMLGregorianCalendar(erBarnet ? FOM_BARNET : FOM))
                                     .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(statsborgerskapPeriode);
    }

    private static Collection<BostedsadressePeriode> hentBostedsadresse(boolean erBarnet) {
        BostedsadressePeriode bostedsadressePeriode = new BostedsadressePeriode();
        bostedsadressePeriode
                .withBostedsadresse(NORSK_ADRESSE)
                .withPeriode(new Periode()
                                     .withFom(DateUtil.convertToXMLGregorianCalendar(erBarnet ? FOM_BARNET : FOM))
                                     .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(bostedsadressePeriode);
    }

    private static Collection<MidlertidigPostadresse> hentMidlertidigAdresse() {
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
