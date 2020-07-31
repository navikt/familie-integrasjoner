package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TilgangskontrollServiceTest {

    private val saksbehandler: JwtToken = mockk(relaxed = true)
    private val jwtTokenClaims: JwtTokenClaims = mockk()
    private val egenAnsattService: EgenAnsattService = mockk(relaxed = true)
    private val tilgangConfig: TilgangConfig = mockk(relaxed = true)
    private val personopplysningerService: PersonopplysningerService = mockk(relaxed = true)

    private val cachedTilgangskontrollService = CachedTilgangskontrollService(egenAnsattService,
                                                                              personopplysningerService,
                                                                              tilgangConfig)
    private var tilgangskontrollService: TilgangskontrollService = TilgangskontrollService(cachedTilgangskontrollService)

    @Test
    fun `tilgang til egen ansatt gir ikke tilgang hvis saksbehandler mangler rollen`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(true)
        every { tilgangConfig.grupper["utvidetTilgang"] }
                .returns(AdRolle("8796", "NAV-Ansatt"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `tilgang til egen ansatt gir tilgang hvis saksbehandler har rollen`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(true)
        every { tilgangConfig.grupper["utvidetTilgang"] }
                .returns(AdRolle("8796", "NAV-Ansatt"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("8796"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `tilgang til egen ansatt gir ok hvis søker ikke er egen ansatt`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { tilgangConfig.grupper["utvidetTilgang"] }
                .returns(AdRolle("8796", "NAV-Ansatt"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
                .returns(personMedAdressebeskyttelse(null))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode6 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { tilgangConfig.grupper["kode6"] }
                .returns(AdRolle("8796", "Strengt fortrolig adresse"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
                .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode7 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { tilgangConfig.grupper["kode7"] }
                .returns(AdRolle("8796", "Fortrolig adresse"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
                .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode6 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("8796"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
                .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG))
        every { tilgangConfig.grupper["kode6"] }
                .returns(AdRolle("8796", "Strengt fortrolig adresse"))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode7 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("8796"))
        every { personopplysningerService.hentPersoninfo("123", any(), any()) }
                .returns(personMedAdressebeskyttelse(ADRESSEBESKYTTELSEGRADERING.FORTROLIG))
        every { tilgangConfig.grupper["kode7"] }
                .returns(AdRolle("8796", "Fortrolig adresse"))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    private fun personMedAdressebeskyttelse(adressebeskyttelsegradering: ADRESSEBESKYTTELSEGRADERING?): Person {
        return Person(navn = "Navn Navnesen",
                      fødselsdato = "1980-01-01",
                      kjønn = "KVINNE",
                      familierelasjoner = emptySet(),
                      adressebeskyttelseGradering = adressebeskyttelsegradering,
                      sivilstand = SIVILSTAND.UGIFT)
    }
}
