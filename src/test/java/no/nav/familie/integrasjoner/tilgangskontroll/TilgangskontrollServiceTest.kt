package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TilgangskontrollServiceTest {

    private val saksbehandler: JwtToken = mockk(relaxed = true)
    private val jwtTokenClaims: JwtTokenClaims = mockk()
    private val egenAnsattService: EgenAnsattService = mockk(relaxed = true)
    private val GRUPPE_UTVIDET_TILGANG = "utvidetTilgang1"
    private val GRUPPE_TILGANG_6 = "kode62"
    private val GRUPPE_TILGANG_7 = "kode73"

    private val tilgangConfig: TilgangConfig =
        TilgangConfig(
            mapOf(
                "utvidet-tilgang" to AdRolle(GRUPPE_UTVIDET_TILGANG, "NAV-Ansatt"),
                "kode6" to AdRolle(GRUPPE_TILGANG_6, "Strengt fortrolig adresse"),
                "kode7" to AdRolle(GRUPPE_TILGANG_7, "Fortrolig adresse")
            )
        )
    private val personopplysningerService: PersonopplysningerService = mockk(relaxed = true)

    private val cachedTilgangskontrollService = CachedTilgangskontrollService(
        egenAnsattService,
        personopplysningerService,
        tilgangConfig
    )
    private var tilgangskontrollService: TilgangskontrollService = TilgangskontrollService(cachedTilgangskontrollService)

    @Test
    fun `tilgang til egen ansatt gir ikke tilgang hvis saksbehandler mangler rollen`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(true)
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))

        assertThat(
            tilgangskontrollService.sjekkTilgang(
                "123",
                saksbehandler
            ).harTilgang
        )
            .isFalse
    }

    @Test
    fun `tilgang til egen ansatt gir tilgang hvis saksbehandler har rollen`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(true)
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) }
            .returns(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.UGRADERT))
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf(GRUPPE_UTVIDET_TILGANG))
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isTrue
    }

    @Test
    fun `tilgang til egen ansatt gir ok hvis søker ikke er egen ansatt`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(false)
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) }
            .returns(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.UGRADERT))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isTrue
    }

    @Test
    fun `hvis kode6 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(false)
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
            .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isFalse
    }

    @Test
    fun `hvis kode7 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(false)
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
            .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isFalse
    }

    @Test
    fun `hvis kode6 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(false)
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf(GRUPPE_TILGANG_6))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
            .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isTrue
    }

    @Test
    fun `hvis kode7 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) }
            .returns(false)
        every { saksbehandler.jwtTokenClaims }
            .returns(jwtTokenClaims)
        every { jwtTokenClaims.get("preferred_username") }
            .returns(listOf("bob"))
        every { jwtTokenClaims.getAsList(any()) }
            .returns(listOf(GRUPPE_TILGANG_7))
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) }
            .returns(Adressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123", saksbehandler).harTilgang)
            .isTrue
    }

    private fun personMedAdressebeskyttelse(adressebeskyttelsegradering: ADRESSEBESKYTTELSEGRADERING?): Person {
        return Person(
            navn = "Navn Navnesen",
            fødselsdato = "1980-01-01",
            kjønn = "KVINNE",
            familierelasjoner = emptySet(),
            adressebeskyttelseGradering = adressebeskyttelsegradering,
            sivilstand = SIVILSTAND.UGIFT
        )
    }
}
