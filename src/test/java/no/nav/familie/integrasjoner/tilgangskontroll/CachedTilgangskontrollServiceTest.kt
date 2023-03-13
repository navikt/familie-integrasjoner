package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CachedTilgangskontrollServiceTest {

    private val egenAnsattService = mockk<EgenAnsattService>()
    private val personopplysningerService = mockk<PersonopplysningerService>()
    private val kode7Id = "6"
    private val kode6Id = "7"

    private val tilgangConfig = TilgangConfig(
        mapOf(
            "kode7" to AdRolle(kode7Id, ""),
            "kode6" to AdRolle(kode6Id, ""),
        ),
    )
    private val cachedTilgangskontrollService = CachedTilgangskontrollService(
        egenAnsattService,
        personopplysningerService,
        tilgangConfig,
    )

    private val jwtToken = mockk<JwtToken>(relaxed = true)
    private val jwtTokenClaims = mockk<JwtTokenClaims>()

    @BeforeEach
    internal fun setUp() {
        every { jwtToken.jwtTokenClaims } returns jwtTokenClaims
        every { jwtTokenClaims.get("preferred_username") }.returns(listOf("bob"))
        every { jwtTokenClaims.getAsList(any()) }.returns(emptyList())
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { egenAnsattService.erEgenAnsatt(any<Set<String>>()) } answers
            { firstArg<Set<String>>().associateWith { false } }
    }

    private fun mockHarKode7() {
        every { jwtTokenClaims.getAsList(any()) }.returns(listOf(kode7Id))
    }

    private fun mockHarKode6() {
        every { jwtTokenClaims.getAsList(any()) }.returns(listOf(kode6Id))
    }

    @Test
    internal fun `har tilgang når det ikke finnes noen adressebeskyttelser for enskild person`() {
        mockHentPersonMedAdressebeskyttelse()
        assertThat(sjekkTilgangTilPerson()).isTrue
        verify(exactly = 1) { egenAnsattService.erEgenAnsatt(any<String>()) }
    }

    @Test
    internal fun `har ikke tilgang når det finnes adressebeskyttelser for enskild person`() {
        mockHentPersonMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPerson()).isFalse
        verify(exactly = 0) { egenAnsattService.erEgenAnsatt(any<String>()) }
    }

    @Test
    internal fun `har ikke tilgang når det ikke finnes noen adressebeskyttelser for enskild person men er ansatt`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns true
        mockHentPersonMedAdressebeskyttelse()
        assertThat(sjekkTilgangTilPerson()).isFalse
        verify(exactly = 1) { egenAnsattService.erEgenAnsatt(any<String>()) }
    }

    @Test
    internal fun `har tilgang når det ikke finnes noen adressebeskyttelser`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns lagPersonMedRelasjoner()
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isTrue
    }

    @Test
    internal fun `har ikke tilgang når søkeren er STRENGT_FORTROLIG og saksbehandler har kode7`() {
        mockHarKode7()
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har tilgang når søkeren er STRENGT_FORTROLIG og saksbehandler har kode6`() {
        mockHarKode6()
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isTrue
    }

    @Test
    internal fun `har ikke tilgang når det finnes adressebeskyttelse for søkeren`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(adressebeskyttelse = ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har ikke tilgang når sivilstand inneholder FORTROLIG`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(sivilstand = ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har ikke tilgang når fullmakt inneholder FORTROLIG`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(fullmakt = ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har ikke tilgang når barn inneholder FORTROLIG`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(barn = ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har ikke tilgang når barnsForeldrer inneholder FORTROLIG`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(barnsForeldrer = ADRESSEBESKYTTELSEGRADERING.FORTROLIG)
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    @Test
    internal fun `har ikke tilgang når sivilstand er egenansatt`() {
        every { personopplysningerService.hentPersonMedRelasjoner(any(), Tema.ENF) } returns
            lagPersonMedRelasjoner(sivilstand = ADRESSEBESKYTTELSEGRADERING.UGRADERT)
        every { egenAnsattService.erEgenAnsatt(any<Set<String>>()) } answers {
            firstArg<Set<String>>().associateWith { it == "sivilstand" }
        }
        assertThat(sjekkTilgangTilPersonMedRelasjoner()).isFalse
    }

    private fun sjekkTilgangTilPersonMedRelasjoner() =
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner("", jwtToken, Tema.ENF).harTilgang

    private fun sjekkTilgangTilPerson() = cachedTilgangskontrollService.sjekkTilgang("", jwtToken, Tema.ENF).harTilgang

    private fun lagPersonMedRelasjoner(
        adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING? = null,
        sivilstand: ADRESSEBESKYTTELSEGRADERING? = null,
        fullmakt: ADRESSEBESKYTTELSEGRADERING? = null,
        barn: ADRESSEBESKYTTELSEGRADERING? = null,
        barnsForeldrer: ADRESSEBESKYTTELSEGRADERING? = null,
    ): PersonMedRelasjoner {
        return PersonMedRelasjoner(
            personIdent = "",
            adressebeskyttelse = adressebeskyttelse,
            sivilstand = lagPersonMedBeskyttelse(sivilstand, "sivilstand"),
            fullmakt = lagPersonMedBeskyttelse(fullmakt, "fullmakt"),
            barn = lagPersonMedBeskyttelse(barn, "barn"),
            barnsForeldrer = lagPersonMedBeskyttelse(barnsForeldrer, "barnsForeldrer"),
        )
    }

    private fun lagPersonMedBeskyttelse(sivilstand: ADRESSEBESKYTTELSEGRADERING?, personIdent: String) =
        sivilstand?.let { listOf(PersonMedAdresseBeskyttelse(personIdent, it)) } ?: emptyList()

    private fun mockHentPersonMedAdressebeskyttelse(adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING = ADRESSEBESKYTTELSEGRADERING.UGRADERT) {
        every { personopplysningerService.hentAdressebeskyttelse(any(), any()) } returns Adressebeskyttelse(adressebeskyttelse)
    }

    private fun mockHentPersonInfo(adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING = ADRESSEBESKYTTELSEGRADERING.UGRADERT) {
        every { personopplysningerService.hentPersoninfo(any(), any(), any()) } returns
            Person(
                fødselsdato = "1980-05-12",
                navn = "navn",
                kjønn = "KVINNE",
                familierelasjoner = emptySet(),
                adressebeskyttelseGradering = adressebeskyttelse,
                sivilstand = SIVILSTAND.UGIFT,
            )
    }
}
