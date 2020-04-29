package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter
import no.nav.familie.integrasjoner.personopplysning.domene.adresse.AdresseType
import no.nav.familie.integrasjoner.personopplysning.domene.adresse.TpsAdresseOversetter
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.Familierelasjon
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.RelasjonsRolleType
import no.nav.familie.integrasjoner.personopplysning.domene.relasjon.SivilstandType
import no.nav.familie.integrasjoner.personopplysning.domene.status.PersonstatusType
import no.nav.familie.integrasjoner.personopplysning.domene.tilhørighet.Landkode
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PersonopplysningerServiceTest {

    private lateinit var personSoapClient: PersonSoapClient
    private lateinit var personopplysningerService: PersonopplysningerService

    @Before
    fun setUp() {
        personSoapClient = PersonopplysningerTestConfig().personConsumerMock()
        personopplysningerService = PersonopplysningerService(personSoapClient,
                                                              TpsOversetter(TpsAdresseOversetter()),
                                                              mock(PdlRestClient::class.java))
    }

    @Test
    fun personhistorikkInfoSkalGiFeilVedUgyldigAktørId() {
        Mockito.`when`(personSoapClient.hentPersonhistorikkResponse(
                ArgumentMatchers.any(HentPersonhistorikkRequest::class.java)))
                .thenThrow(OppslagException("feil", "feil", OppslagException.Level.MEDIUM))
        Assertions.assertThatThrownBy {
            personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personHistorikkSkalGiFeilVedSikkerhetsbegrensning() {
        Mockito.`when`(personSoapClient.hentPersonhistorikkResponse(
                ArgumentMatchers.any(HentPersonhistorikkRequest::class.java)))
                .thenThrow(OppslagException("feil", "feil", OppslagException.Level.MEDIUM))
        Assertions.assertThatThrownBy {
            personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personinfoSkalGiFeilVedUgyldigAktørId() {
        Mockito.`when`(personSoapClient.hentPersonResponse(
                ArgumentMatchers.any(HentPersonRequest::class.java)))
                .thenThrow(OppslagException("feil", "feil", OppslagException.Level.MEDIUM))
        Assertions.assertThatThrownBy {
            personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        }
                .isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personinfoSkalGiFeilVedSikkerhetsbegrensning() {
        Mockito.`when`(personSoapClient.hentPersonResponse(
                ArgumentMatchers.any(HentPersonRequest::class.java)))
                .thenThrow(OppslagException("feil", "feil", OppslagException.Level.MEDIUM))
        Assertions.assertThatThrownBy {
            personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun skalKonvertereResponsTilPersonInfo() {
        val response =
                personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        val forventetFødselsdato = LocalDate.parse("1990-01-01")
        val barn =
                response.familierelasjoner.find { p: Familierelasjon -> p.relasjonsrolle == RelasjonsRolleType.BARN }
        val ektefelle =
                response.familierelasjoner.find { p: Familierelasjon -> p.relasjonsrolle == RelasjonsRolleType.EKTE }
        Assertions.assertThat(response.personIdent.id)
                .isEqualTo(PERSONIDENT)
        Assertions.assertThat(response.statsborgerskap.erNorge()).isTrue()
        Assertions.assertThat(response.sivilstand).isEqualTo(SivilstandType.GIFT)
        Assertions.assertThat(response.alder).isEqualTo(ChronoUnit.YEARS.between(forventetFødselsdato, LocalDate.now()))
        Assertions.assertThat(response.adresseInfoList).hasSize(1)
        Assertions.assertThat(response.personstatus).isEqualTo(PersonstatusType.BOSA)
        Assertions.assertThat(response.geografiskTilknytning).isEqualTo("0315")
        Assertions.assertThat(response.fødselsdato).isEqualTo(forventetFødselsdato)
        Assertions.assertThat(response.dødsdato).isNull()
        Assertions.assertThat(response.diskresjonskode).isNull()
        Assertions.assertThat(response.adresseLandkode).isEqualTo("NOR")
        Assertions.assertThat(response.familierelasjoner).hasSize(2)
        Assertions.assertThat(barn).isNotNull
        Assertions.assertThat(barn!!.harSammeBosted).isTrue()
        Assertions.assertThat(ektefelle).isNotNull
        Assertions.assertThat(ektefelle!!.harSammeBosted).isTrue()
    }

    @Test
    fun skalKonvertereResponsTilPersonhistorikkInfo() {
        val response =
                personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)

        Assertions.assertThat(response.personIdent.id).isEqualTo(PERSONIDENT)
        Assertions.assertThat(response.statsborgerskaphistorikk).hasSize(1)
        Assertions.assertThat(response.personstatushistorikk).hasSize(1)
        Assertions.assertThat(response.adressehistorikk).hasSize(2)
        Assertions.assertThat(response.statsborgerskaphistorikk[0].tilhørendeLand).isEqualTo(Landkode.NORGE)
        Assertions.assertThat(response.statsborgerskaphistorikk[0].periode.fom).isEqualTo(FOM)
        Assertions.assertThat(response.personstatushistorikk[0].personstatus).isEqualTo(PersonstatusType.BOSA)
        Assertions.assertThat(response.personstatushistorikk[0].periode.tom).isEqualTo(TOM)
        Assertions.assertThat(response.adressehistorikk[0].adresse.adresseType).isEqualTo(AdresseType.BOSTEDSADRESSE)
        Assertions.assertThat(response.adressehistorikk[0].adresse.land).isEqualTo(Landkode.NORGE.kode)
        Assertions.assertThat(response.adressehistorikk[0].adresse.adresselinje1).isEqualTo("Sannergata 2")
        Assertions.assertThat(response.adressehistorikk[0].adresse.postnummer).isEqualTo("0560")
        Assertions.assertThat(response.adressehistorikk[0].adresse.poststed).isEqualTo("OSLO")
        Assertions.assertThat(response.adressehistorikk[1].adresse.adresseType)
                .isEqualTo(AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND)
        Assertions.assertThat(response.adressehistorikk[1].adresse.land).isEqualTo("SWE")
        Assertions.assertThat(response.adressehistorikk[1].adresse.adresselinje1).isEqualTo("TEST 1")
    }

    companion object {
        private const val PERSONIDENT = SøknadTestdata.farPersonident
        private val TOM = LocalDate.now()
        private val FOM = TOM.minusYears(5)
    }
}