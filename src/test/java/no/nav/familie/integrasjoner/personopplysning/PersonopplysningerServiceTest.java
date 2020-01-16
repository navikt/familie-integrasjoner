package no.nav.familie.integrasjoner.personopplysning;

import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo;
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter;
import no.nav.familie.integrasjoner.personopplysning.domene.adresse.AdresseType;
import no.nav.familie.integrasjoner.personopplysning.domene.adresse.TpsAdresseOversetter;
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.Familierelasjon;
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.RelasjonsRolleType;
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.SivilstandType;
import no.nav.familie.integrasjoner.personopplysning.domene.status.PersonstatusType;
import no.nav.familie.integrasjoner.personopplysning.domene.tilhørighet.Landkode;
import no.nav.familie.integrasjoner.personopplysning.internal.PersonConsumer;
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PersonopplysningerServiceTest {

    private static final String PERSONIDENT = SøknadTestdata.farPersonident;
    private static final LocalDate TOM = LocalDate.now();
    private static final LocalDate FOM = TOM.minusYears(5);

    private PersonConsumer personConsumer;
    private PersonopplysningerService personopplysningerService;

    @Before
    public void setUp() throws Exception {
        personConsumer = new PersonopplysningerTestConfig().personConsumerMock();
        personopplysningerService =
                new PersonopplysningerService(this.personConsumer, new TpsOversetter(new TpsAdresseOversetter()));
    }

    @Test
    public void personhistorikkInfoSkalGiFeilVedUgyldigAktørId() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class)))
                .thenThrow(new HentPersonhistorikkPersonIkkeFunnet("Feil", any(PersonIkkeFunnet.class)));

        assertThatThrownBy(() -> personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)).isInstanceOf(
                HttpClientErrorException.NotFound.class);
    }

    @Test
    public void personHistorikkSkalGiFeilVedSikkerhetsbegrensning() throws Exception {
        when(personConsumer.hentPersonhistorikkResponse(any(HentPersonhistorikkRequest.class)))
                .thenThrow(new HentPersonhistorikkSikkerhetsbegrensning("Feil", any(Sikkerhetsbegrensning.class)));

        assertThatThrownBy(() -> personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)).isInstanceOf(
                HttpClientErrorException.Forbidden.class);
    }

    @Test
    public void personinfoSkalGiFeilVedUgyldigAktørId() throws Exception {
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class)))
                .thenThrow(new HentPersonPersonIkkeFunnet("Feil", any(PersonIkkeFunnet.class)));

        assertThatThrownBy(() -> personopplysningerService.hentPersoninfoFor(PERSONIDENT))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    public void personinfoSkalGiFeilVedSikkerhetsbegrensning() throws Exception {
        when(personConsumer.hentPersonResponse(any(HentPersonRequest.class)))
                .thenThrow(new HentPersonSikkerhetsbegrensning("Feil", any(Sikkerhetsbegrensning.class)));

        assertThatThrownBy(() -> personopplysningerService.hentPersoninfoFor(PERSONIDENT))
                .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    public void skalKonvertereResponsTilPersonInfo() {
        Personinfo response = personopplysningerService.hentPersoninfoFor(PERSONIDENT);

        LocalDate forventetFødselsdato = LocalDate.parse("1990-01-01");

        Familierelasjon barn = response.getFamilierelasjoner().stream()
                                       .filter(p -> p.getRelasjonsrolle() == RelasjonsRolleType.BARN)
                                       .findFirst().orElse(null);
        Familierelasjon ektefelle = response.getFamilierelasjoner().stream()
                                            .filter(p -> p.getRelasjonsrolle() == RelasjonsRolleType.EKTE)
                                            .findFirst().orElse(null);

        assertThat(response.getPersonIdent().getId()).isEqualTo(PERSONIDENT);
        assertThat(response.getStatsborgerskap().erNorge()).isTrue();
        assertThat(response.getSivilstand()).isEqualTo(SivilstandType.GIFT);
        assertThat(response.getAlder()).isEqualTo(ChronoUnit.YEARS.between(forventetFødselsdato, LocalDate.now()));
        Assertions.assertThat(response.getAdresseInfoList()).hasSize(1);
        Assertions.assertThat(response.getPersonstatus()).isEqualTo(PersonstatusType.BOSA);
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
        PersonhistorikkInfo response = personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM);

        assertThat(response.getPersonIdent().getId()).isEqualTo(PERSONIDENT);
        Assertions.assertThat(response.getStatsborgerskaphistorikk()).hasSize(1);
        Assertions.assertThat(response.getPersonstatushistorikk()).hasSize(1);
        Assertions.assertThat(response.getAdressehistorikk()).hasSize(2);

        assertThat(response.getStatsborgerskaphistorikk().get(0).getTilhørendeLand()).isEqualTo(Landkode.NORGE);
        Assertions.assertThat(response.getStatsborgerskaphistorikk().get(0).getPeriode().getFom()).isEqualTo(FOM);

        Assertions.assertThat(response.getPersonstatushistorikk().get(0).getPersonstatus()).isEqualTo(PersonstatusType.BOSA);
        Assertions.assertThat(response.getPersonstatushistorikk().get(0).getPeriode().getTom()).isEqualTo(TOM);

        Assertions.assertThat(response.getAdressehistorikk().get(0).getAdresse().getAdresseType())
                  .isEqualTo(AdresseType.BOSTEDSADRESSE);
        Assertions.assertThat(response.getAdressehistorikk().get(0).getAdresse().getLand()).isEqualTo(Landkode.NORGE.getKode());
        Assertions.assertThat(response.getAdressehistorikk().get(0).getAdresse().getAdresselinje1()).isEqualTo("Sannergata 2");
        Assertions.assertThat(response.getAdressehistorikk().get(0).getAdresse().getPostnummer()).isEqualTo("0560");
        Assertions.assertThat(response.getAdressehistorikk().get(0).getAdresse().getPoststed()).isEqualTo("OSLO");

        Assertions.assertThat(response.getAdressehistorikk().get(1).getAdresse().getAdresseType())
                  .isEqualTo(AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND);
        Assertions.assertThat(response.getAdressehistorikk().get(1).getAdresse().getLand()).isEqualTo("SWE");
        Assertions.assertThat(response.getAdressehistorikk().get(1).getAdresse().getAdresselinje1()).isEqualTo("TEST 1");
    }

}
