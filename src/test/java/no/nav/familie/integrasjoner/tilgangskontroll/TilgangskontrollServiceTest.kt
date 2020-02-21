package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.azure.domene.Gruppe
import no.nav.familie.integrasjoner.azure.domene.Grupper
import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate

@RunWith(SpringRunner::class)
class TilgangskontrollServiceTest {

    private val azureGraphRestClient: AzureGraphRestClient = mockk(relaxed = true)
    private val saksbehandler = Saksbehandler("", "sdfsdf")
    private val egenAnsattService: EgenAnsattService = mockk(relaxed = true)
    private val personopplysningerService: PersonopplysningerService = mockk(relaxed = true)

    private var tilgangskontrollService: TilgangskontrollService = TilgangskontrollService(azureGraphRestClient,
                                                                                           egenAnsattService,
                                                                                           personopplysningerService)

    @Test
    fun `tilgang til egen ansatt gir ikke tilgang hvis saksbehandler mangler rollen`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(true)

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoUtenKode6og7()).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `tilgang til egen ansatt gir tilgang hvis saksbehandler har rollen`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(true)
        every { azureGraphRestClient.hentGrupper() }
                .returns(Grupper(listOf(Gruppe("1", "0000-GA-GOSYS_UTVIDET"))))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoUtenKode6og7()).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `tilgang til egen ansatt gir ok hvis søker ikke er egen ansatt`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoUtenKode6og7()).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode6 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoMedKode6()).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode7 har ikke saksbehandler uten rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoMedKode7()).harTilgang)
                .isEqualTo(Tilgang(false).harTilgang)
    }

    @Test
    fun `hvis kode6 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { azureGraphRestClient.hentGrupper() }
                .returns(Grupper(listOf(Gruppe("1", "0000-GA-GOSYS_KODE6"))))


        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoMedKode6()).harTilgang)
                .isEqualTo(Tilgang(true).harTilgang)
    }

    @Test
    fun `hvis kode7 har saksbehandler med rollen tilgang`() {
        every { egenAnsattService.erEgenAnsatt(any()) }
                .returns(false)
        every { azureGraphRestClient.hentGrupper() }
                .returns(Grupper(listOf(Gruppe("1", "0000-GA-GOSYS_KODE7"))))

        assertThat(tilgangskontrollService.sjekkTilgang("123",
                                                        saksbehandler,
                                                        personinfoMedKode7()).harTilgang)
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
                .medDiskresjonsKode(TilgangskontrollService.DISKRESJONSKODE_KODE6)
                .build()
    }

    private fun personinfoMedKode7(): Personinfo {
        return Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(NAVN)
                .medDiskresjonsKode(TilgangskontrollService.DISKRESJONSKODE_KODE7)
                .build()
    }

    companion object {
        private const val NAVN = "Navn Navnesen"
        private const val FNR = "fnr"
        private val PERSON_IDENT = PersonIdent(FNR)
    }
}
