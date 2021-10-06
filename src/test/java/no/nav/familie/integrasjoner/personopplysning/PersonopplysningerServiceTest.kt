package no.nav.familie.integrasjoner.personopplysning

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.FORELDERBARNRELASJONROLLE
import no.nav.familie.integrasjoner.personopplysning.internal.Fullmakt
import no.nav.familie.integrasjoner.personopplysning.internal.PdlForelderBarnRelasjon
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Sivilstand
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PersonopplysningerServiceTest {

    private lateinit var personSoapClient: PersonSoapClient
    private lateinit var personopplysningerService: PersonopplysningerService

    private val pdlRestClient = mockk<PdlRestClient>()

    @BeforeEach
    fun setUp() {
        personSoapClient = PersonopplysningerTestConfig().personConsumerMock()
        personopplysningerService = PersonopplysningerService(personSoapClient,
                                                              TpsOversetter(TpsAdresseOversetter()),
                                                              pdlRestClient)
    }

    @Test
    fun personhistorikkInfoSkalGiFeilVedUgyldigAktørId() {
        every { personSoapClient.hentPersonhistorikkResponse(any()) } throws
                OppslagException("feil", "feil", OppslagException.Level.MEDIUM)
        assertThatThrownBy {
            personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personHistorikkSkalGiFeilVedSikkerhetsbegrensning() {
        every { personSoapClient.hentPersonhistorikkResponse(any()) } throws
                OppslagException("feil", "feil", OppslagException.Level.MEDIUM)
        assertThatThrownBy {
            personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personinfoSkalGiFeilVedUgyldigAktørId() {
        every { personSoapClient.hentPersonResponse(any()) } throws
                OppslagException("feil", "feil", OppslagException.Level.MEDIUM)
        assertThatThrownBy {
            personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        }
                .isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun personinfoSkalGiFeilVedSikkerhetsbegrensning() {
        every { personSoapClient.hentPersonResponse(any()) } throws
                OppslagException("feil", "feil", OppslagException.Level.MEDIUM)
        assertThatThrownBy {
            personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        }.isInstanceOf(OppslagException::class.java)
    }

    @Test
    fun skalKonvertereResponsTilPersonInfo() {
        val response = personopplysningerService.hentPersoninfoFor(PERSONIDENT)
        val forventetFødselsdato = LocalDate.parse("1990-01-01")
        val barn = response.familierelasjoner.find { p: Familierelasjon -> p.relasjonsrolle == RelasjonsRolleType.BARN }
        val ektefelle = response.familierelasjoner.find { p: Familierelasjon -> p.relasjonsrolle == RelasjonsRolleType.EKTE }
        assertThat(response.personIdent.id).isEqualTo(PERSONIDENT)
        assertThat(response.statsborgerskap.erNorge()).isTrue()
        assertThat(response.sivilstand).isEqualTo(SivilstandType.GIFT)
        assertThat(response.alder).isEqualTo(ChronoUnit.YEARS.between(forventetFødselsdato, LocalDate.now()))
        assertThat(response.adresseInfoList).hasSize(1)
        assertThat(response.personstatus).isEqualTo(PersonstatusType.BOSA)
        assertThat(response.geografiskTilknytning).isEqualTo("0315")
        assertThat(response.fødselsdato).isEqualTo(forventetFødselsdato)
        assertThat(response.dødsdato).isNull()
        assertThat(response.diskresjonskode).isNull()
        assertThat(response.adresseLandkode).isEqualTo("NOR")
        assertThat(response.familierelasjoner).hasSize(2)
        assertThat(barn).isNotNull
        assertThat(barn!!.harSammeBosted).isTrue()
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.harSammeBosted).isTrue()
    }

    @Test
    fun skalKonvertereResponsTilPersonhistorikkInfo() {
        val response = personopplysningerService.hentHistorikkFor(PERSONIDENT, FOM, TOM)

        assertThat(response.personIdent.id).isEqualTo(PERSONIDENT)
        assertThat(response.statsborgerskaphistorikk).hasSize(1)
        assertThat(response.personstatushistorikk).hasSize(1)
        assertThat(response.adressehistorikk).hasSize(2)
        assertThat(response.statsborgerskaphistorikk[0].tilhørendeLand).isEqualTo(Landkode.NORGE)
        assertThat(response.statsborgerskaphistorikk[0].periode.fom).isEqualTo(FOM)
        assertThat(response.personstatushistorikk[0].personstatus).isEqualTo(PersonstatusType.BOSA)
        assertThat(response.personstatushistorikk[0].periode.tom).isEqualTo(TOM)
        assertThat(response.adressehistorikk[0].adresse.adresseType).isEqualTo(AdresseType.BOSTEDSADRESSE)
        assertThat(response.adressehistorikk[0].adresse.land).isEqualTo(Landkode.NORGE.kode)
        assertThat(response.adressehistorikk[0].adresse.adresselinje1).isEqualTo("Sannergata 2")
        assertThat(response.adressehistorikk[0].adresse.postnummer).isEqualTo("0560")
        assertThat(response.adressehistorikk[0].adresse.poststed).isEqualTo("OSLO")
        assertThat(response.adressehistorikk[1].adresse.adresseType)
                .isEqualTo(AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND)
        assertThat(response.adressehistorikk[1].adresse.land).isEqualTo("SWE")
        assertThat(response.adressehistorikk[1].adresse.adresselinje1).isEqualTo("TEST 1")
    }

    @Test
    fun `hentPersonMedRelasjoner skal kalle på pdl 3 ganger, hovedpersonen, relasjonene og barnets andre forelder`() {
        val hovedPerson = "1" to
                lagPdlPersonMedRelasjoner(familierelasjoner = listOf(PdlForelderBarnRelasjon("2",
                                                                                             FORELDERBARNRELASJONROLLE.BARN)),
                                          sivilstand = listOf(Sivilstand(SIVILSTAND.GIFT, "3")),
                                          fullmakt = listOf(Fullmakt("4")))
        val barn = lagPdlPersonMedRelasjoner(familierelasjoner = listOf(PdlForelderBarnRelasjon("22",
                                                                                                FORELDERBARNRELASJONROLLE.FAR)))
        every { pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(any(), any()) } answers {
            firstArg<List<String>>().map { it to if (it == "2") barn else lagPdlPersonMedRelasjoner() }.toMap()
        }
        every { pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("1"), any()) } returns mapOf(hovedPerson)

        val hentPersonMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner("1", Tema.ENF)

        assertThat(hentPersonMedRelasjoner.barn.single().personIdent).isEqualTo("2")
        assertThat(hentPersonMedRelasjoner.barnsForeldrer.single().personIdent).isEqualTo("22")
        assertThat(hentPersonMedRelasjoner.sivilstand.single().personIdent).isEqualTo("3")
        assertThat(hentPersonMedRelasjoner.fullmakt.single().personIdent).isEqualTo("4")

        verify(exactly = 1) {
            pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("1"), any())
            pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("2", "3", "4"), any())
            pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("22"), any())
        }
    }

    private fun lagPdlPersonMedRelasjoner(familierelasjoner: List<PdlForelderBarnRelasjon> = emptyList(),
                                          sivilstand: List<Sivilstand> = emptyList(),
                                          fullmakt: List<Fullmakt> = emptyList(),
                                          adressebeskyttelse: List<Adressebeskyttelse> = emptyList()) =
            PdlPersonMedRelasjonerOgAdressebeskyttelse(familierelasjoner, sivilstand, fullmakt, adressebeskyttelse)

    companion object {

        private const val PERSONIDENT = SøknadTestdata.farPersonident
        private val TOM = LocalDate.now()
        private val FOM = TOM.minusYears(5)
    }
}