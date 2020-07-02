package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.azure.domene.Gruppe
import no.nav.familie.integrasjoner.azure.domene.Grupper
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
class TilgangskontrollServiceTest {

    private val azureGraphRestClient: AzureGraphRestClient = mockk(relaxed = true)
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
                .returns(AdRolle("8796", "Mangler tilgang egen ansatt"))
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
                .returns(AdRolle("8796", "Mangler tilgang egen ansatt"))
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

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode6 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { tilgangConfig.grupper["kode6"] }
                .returns(AdRolle("8796", "Mangler tilgang egen ansatt"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123") }
                .returns(personinfoMedKode6())

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode7 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { tilgangConfig.grupper["kode7"] }
                .returns(AdRolle("8796", "Mangler tilgang egen ansatt"))
        every { saksbehandler.jwtTokenClaims }
                .returns(jwtTokenClaims)
        every { jwtTokenClaims.getAsList(any()) }
                .returns(listOf("id1"))
        every { jwtTokenClaims.get("preferred_username") }
                .returns(listOf("bob"))
        every { personopplysningerService.hentPersoninfo("123") }
                .returns(personinfoMedKode7())

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode6 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { azureGraphRestClient.hentGrupper() }
                .returns(Grupper(listOf(Gruppe("1", "0000-GA-GOSYS_KODE6"))))


        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode7 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { azureGraphRestClient.hentGrupper() }
                .returns(Grupper(listOf(Gruppe("1", "0000-GA-GOSYS_KODE7"))))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    private fun personinfoUtenKode6og7(): Personinfo {
        return Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(NAVN)
                .medDiskresjonsKode("VANLIG_PERSON")
                .build()
    }

    private fun personinfoMedKode6(): Personinfo {
        return Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(NAVN)
                .medDiskresjonsKode(CachedTilgangskontrollService.DISKRESJONSKODE_KODE6)
                .build()
    }

    private fun personinfoMedKode7(): Personinfo {
        return Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(NAVN)
                .medDiskresjonsKode(CachedTilgangskontrollService.DISKRESJONSKODE_KODE7)
                .build()
    }

    companion object {
        private const val NAVN = "Navn Navnesen"
        private const val FNR = "fnr"
        private val PERSON_IDENT = PersonIdent(FNR)
    }
}
