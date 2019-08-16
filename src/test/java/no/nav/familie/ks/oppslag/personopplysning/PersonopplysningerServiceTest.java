package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.TpsOversetter;
import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.AdresseType;
import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.TpsAdresseOversetter;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.Familierelasjon;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.RelasjonsRolleType;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.SivilstandType;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusType;
import no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet.Landkode;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PersonopplysningerServiceTest {

    private static final String AKTØR_ID = "1000011111111";
    private static final LocalDate TOM = LocalDate.now();
    private static final LocalDate FOM = TOM.minusYears(5);

    private PersonConsumer personConsumer;
    private PersonopplysningerService personopplysningerService;

    @Before
    public void setUp() throws Exception {
        personConsumer = new PersonopplysningerTestConfig().personConsumerMock();
        personopplysningerService = new PersonopplysningerService(this.personConsumer, new TpsOversetter(new TpsAdresseOversetter()));
    }

    @Test
    public void skalReturnereTomPersonhistorikkInfoVedUgyldigAktørId() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class)))
                .thenThrow(new HentPersonhistorikkPersonIkkeFunnet("Feil", any(PersonIkkeFunnet.class)));

        PersonhistorikkInfo response = personopplysningerService.hentHistorikkFor(new AktørId(AKTØR_ID), FOM, TOM);

        assertThat(response.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(response.getAdressehistorikk()).isNullOrEmpty();
        assertThat(response.getPersonstatushistorikk()).isNullOrEmpty();
        assertThat(response.getStatsborgerskaphistorikk()).isNullOrEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void personHistorikkSkalGiFeilVedSikkerhetsbegrensning() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class)))
                .thenThrow(new HentPersonhistorikkSikkerhetsbegrensning("Feil", any(Sikkerhetsbegrensning.class)));

        personopplysningerService.hentHistorikkFor(new AktørId(AKTØR_ID), FOM, TOM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void personinfoSkalGiFeilVedUgyldigAktørId() throws Exception {
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class)))
                .thenThrow(new HentPersonPersonIkkeFunnet("Feil", any(PersonIkkeFunnet.class)));

        personopplysningerService.hentPersoninfoFor(new AktørId(AKTØR_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void personinfoSkalGiFeilVedSikkerhetsbegrensning() throws Exception {
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class)))
                .thenThrow(new HentPersonSikkerhetsbegrensning("Feil", any(Sikkerhetsbegrensning.class)));

        personopplysningerService.hentPersoninfoFor(new AktørId(AKTØR_ID));
    }

    @Test
    public void skalKonvertereResponsTilPersonInfo() {
        Personinfo response = personopplysningerService.hentPersoninfoFor(new AktørId(AKTØR_ID));

        LocalDate forventetFødselsdato = LocalDate.parse("1990-01-01");

        Familierelasjon barn = response.getFamilierelasjoner().stream()
                .filter(p -> p.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
                .findFirst().orElse(null);
        Familierelasjon ektefelle = response.getFamilierelasjoner().stream()
                .filter(p -> p.getRelasjonsrolle().equals(RelasjonsRolleType.EKTE))
                .findFirst().orElse(null);

        assertThat(response.getAktørId().getId()).isEqualTo(AKTØR_ID);
        assertThat(response.getStatsborgerskap().erNorge()).isTrue();
        assertThat(response.getSivilstand()).isEqualTo(SivilstandType.GIFT);
        assertThat(response.getAlder()).isEqualTo(29);
        assertThat(response.getAdresseInfoList()).hasSize(1);
        assertThat(response.getPersonstatus()).isEqualTo(PersonstatusType.BOSA);
        assertThat(response.getGeografiskTilknytning()).isEqualTo("0315");
        assertThat(response.getFødselsdato()).isEqualTo(forventetFødselsdato);
        assertThat(response.getDødsdato()).isNull();
        assertThat(response.getDiskresjonskode()).isNull();
        assertThat(response.getAdresseLandkode()).isEqualTo("NOR");

        assertThat(response.getFamilierelasjoner()).hasSize(2);
        assertThat(barn).isNotNull();
        assertThat(barn.getHarSammeBosted()).isTrue();
        assertThat(ektefelle).isNotNull();
        assertThat(ektefelle.getHarSammeBosted()).isTrue();
    }

    @Test
    public void skalKonvertereResponsTilPersonhistorikkInfo() {
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

}