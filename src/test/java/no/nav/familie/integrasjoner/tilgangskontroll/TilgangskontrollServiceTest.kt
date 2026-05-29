package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.UGRADERT
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TilgangskontrollServiceTest {
    private val egenAnsattService: EgenAnsattService = mockk(relaxed = true)

    private val tilgangConfig: TilgangConfig =
        TilgangConfig(
            egenAnsatt = AdRolle(GRUPPE_EGEN_ANSATT, "NAV-Ansatt"),
            kode6 = AdRolle(GRUPPE_TILGANG_6, "Strengt fortrolig adresse"),
            kode7 = AdRolle(GRUPPE_TILGANG_7, "Fortrolig adresse"),
        )
    private val personopplysningerService: PersonopplysningerService = mockk(relaxed = true)

    private val cachedTilgangskontrollService =
        CachedTilgangskontrollService(
            egenAnsattService,
            personopplysningerService,
            tilgangConfig,
        )
    private var tilgangskontrollService: TilgangskontrollService = TilgangskontrollService(cachedTilgangskontrollService)

    @BeforeEach
    fun setup() {
        mockToken(tilganger = emptyList())
    }

    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `tilgang til egen ansatt gir ikke tilgang hvis saksbehandler mangler rollen`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns true

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isFalse
    }

    @Test
    fun `tilgang til egen ansatt gir tilgang hvis saksbehandler har rollen`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns true
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) } returns Adressebeskyttelse(UGRADERT)

        mockToken(tilganger = listOf(GRUPPE_EGEN_ANSATT))

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isTrue
    }

    @Test
    fun `tilgang til egen ansatt gir ok hvis søker ikke er egen ansatt`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) } returns Adressebeskyttelse(UGRADERT)

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isTrue
    }

    @Test
    fun `hvis kode6 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { personopplysningerService.hentPersoninfo("123", any()) } returns personMedAdressebeskyttelse(STRENGT_FORTROLIG)

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isFalse
    }

    @Test
    fun `hvis kode7 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { personopplysningerService.hentPersoninfo("123", any()) } returns personMedAdressebeskyttelse(FORTROLIG)

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isFalse
    }

    @Test
    fun `hvis kode6 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { personopplysningerService.hentPersoninfo("123", any()) } returns personMedAdressebeskyttelse(STRENGT_FORTROLIG)

        mockToken(tilganger = listOf(GRUPPE_TILGANG_6))

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isTrue
    }

    @Test
    fun `hvis kode7 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { personopplysningerService.hentAdressebeskyttelse("123", any()) } returns Adressebeskyttelse(FORTROLIG)

        mockToken(tilganger = listOf(GRUPPE_TILGANG_7))

        assertThat(tilgangskontrollService.sjekkTilgang("123").harTilgang).isTrue
    }

    private fun mockToken(tilganger: List<String>) {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("header", "header value")
                .claim("groups", tilganger)
                .build()
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.setContext(securityContext)
    }

    private fun personMedAdressebeskyttelse(adressebeskyttelsegradering: ADRESSEBESKYTTELSEGRADERING?): Person =
        Person(
            navn = "Navn Navnesen",
            adressebeskyttelseGradering = adressebeskyttelsegradering,
        )

    companion object {
        private const val GRUPPE_EGEN_ANSATT = "utvidetTilgang1"
        private const val GRUPPE_TILGANG_6 = "kode62"
        private const val GRUPPE_TILGANG_7 = "kode73"
    }
}
