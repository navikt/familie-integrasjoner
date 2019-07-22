package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.TpsOversetter;
import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.AdresseType;
import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.TpsAdresseOversetter;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusType;
import no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet.Landkode;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonopplysningerServiceTest {

    private static final String AKTØR_ID = "1000011111111";
    private static final LocalDate TOM = LocalDate.now();
    private static final LocalDate FOM = TOM.minusYears(5);
    private static final Landkoder NORGE = new Landkoder().withValue("NOR");


    private PersonopplysningerService personopplysningerService;
    private PersonConsumer personConsumer = mock(PersonConsumer.class);

    @Before
    public void setUp() {
        personopplysningerService = new PersonopplysningerService(personConsumer, new TpsOversetter(new TpsAdresseOversetter()));
    }

    @Test
    public void skalReturnereTomPersonhistorikkInfoVedUgyldigAktørId() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class))).thenThrow(new HentPersonhistorikkPersonIkkeFunnet("Feil", any(PersonIkkeFunnet.class)));

        PersonhistorikkInfo response = personopplysningerService.hentHistorikkFor(new AktørId(AKTØR_ID), FOM, TOM);

        assertThat(response.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(response.getAdressehistorikk()).isNullOrEmpty();
        assertThat(response.getPersonstatushistorikk()).isNullOrEmpty();
        assertThat(response.getStatsborgerskaphistorikk()).isNullOrEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void skalGiFeilVedSikkerhetsbegrensning() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class))).thenThrow(new HentPersonhistorikkSikkerhetsbegrensning("Feil", any(Sikkerhetsbegrensning.class)));

        personopplysningerService.hentHistorikkFor(new AktørId(AKTØR_ID), FOM, TOM);
    }

    @Test
    public void skalKonvertereResponsTilPersonhistorikkInfo() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class))).thenReturn(hentResponse());

        PersonhistorikkInfo response = personopplysningerService.hentHistorikkFor(new AktørId(AKTØR_ID), FOM, TOM);

        assertThat(response.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(response.getStatsborgerskaphistorikk()).hasSize(1);
        assertThat(response.getPersonstatushistorikk()).hasSize(1);
        assertThat(response.getAdressehistorikk()).hasSize(2);

        assertThat(response.getStatsborgerskaphistorikk().get(0).getTilhørendeLand()).isEqualTo(Landkode.NORGE);
        assertThat(response.getStatsborgerskaphistorikk().get(0).getPeriode().getFom()).isEqualTo(FOM);

        assertThat(response.getPersonstatushistorikk().get(0).getPersonstatus()).isEqualTo(PersonstatusType.BOSA);
        assertThat(response.getPersonstatushistorikk().get(0).getPeriode().getTom()).isEqualTo(TOM);

        assertThat(response.getAdressehistorikk().get(0).getAdresse().getAdresseType()).isEqualTo(AdresseType.BOSTEDSADRESSE);
        assertThat(response.getAdressehistorikk().get(0).getAdresse().getLand()).isEqualTo(Landkode.NORGE.getKode());
        assertThat(response.getAdressehistorikk().get(0).getAdresse().getAdresselinje1()).isEqualTo("Sannergata 2");
        assertThat(response.getAdressehistorikk().get(0).getAdresse().getPostnummer()).isEqualTo("0560");
        assertThat(response.getAdressehistorikk().get(0).getAdresse().getPoststed()).isEqualTo("UDEFINERT");

        assertThat(response.getAdressehistorikk().get(1).getAdresse().getAdresseType()).isEqualTo(AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND);
        assertThat(response.getAdressehistorikk().get(1).getAdresse().getLand()).isEqualTo("SWE");
        assertThat(response.getAdressehistorikk().get(1).getAdresse().getAdresselinje1()).isEqualTo("TEST 1");
    }

    private HentPersonhistorikkResponse hentResponse() {
        HentPersonhistorikkResponse response = new HentPersonhistorikkResponse();
        response.setAktoer(new AktoerId().withAktoerId(AKTØR_ID));
        response
                .withStatsborgerskapListe(hentStatsborgerskap())
                .withPersonstatusListe(hentPersonstatus())
                .withBostedsadressePeriodeListe(hentBostedsadresse())
                .withMidlertidigAdressePeriodeListe(hentMidlertidigAdresse());

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

    private Collection<PersonstatusPeriode> hentPersonstatus() {
        PersonstatusPeriode personstatusPeriode = new PersonstatusPeriode();
        personstatusPeriode
                .withPersonstatus(new Personstatuser().withValue("BOSA"))
                .withPeriode(new Periode()
                        .withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                        .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)));

        return Collections.singletonList(personstatusPeriode);

    }

    private Collection<BostedsadressePeriode> hentBostedsadresse() {
        BostedsadressePeriode bostedsadressePeriode = new BostedsadressePeriode();
        bostedsadressePeriode
                .withBostedsadresse(new Bostedsadresse()
                        .withStrukturertAdresse(new Gateadresse()
                                .withGatenavn("Sannergata")
                                .withHusnummer(2)
                                .withPoststed(new Postnummer().withValue("0560"))
                                .withLandkode(NORGE)))
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
