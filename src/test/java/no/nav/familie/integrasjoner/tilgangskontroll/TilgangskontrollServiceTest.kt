package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import java.lang.Boolean
import java.time.LocalDate

@RunWith(SpringRunner::class)
class TilgangskontrollServiceTest {

    @MockBean
    private val egenAnsattService: EgenAnsattService? = null
    private var tilgangsKontrollService: TilgangsKontrollService? = null
    @MockBean
    private val azureGraphRestClient: AzureGraphRestClient? = null
    private val saksbehandler = Saksbehandler("", "sdfsdf", emptyList())
    @Before fun setUp() {
        tilgangsKontrollService = TilgangsKontrollService(azureGraphRestClient!!, egenAnsattService!!)
    }

    @Test fun tilgang_til_egen_ansatt_gir_ikke_tilgang_hvis_saksbehandler_mangler_rollen() {
        Mockito.`when`(egenAnsattService!!.erEgenAnsatt(ArgumentMatchers.any(String::class.java)))
                .thenReturn(Boolean.TRUE)
        Assertions.assertThat(tilgangsKontrollService!!.sjekkTilgang("123",
                                                                     saksbehandler,
                                                                     personinfoUtenKode6og7())!!.isHarTilgang)
                .isEqualTo(Tilgang().withHarTilgang(false).isHarTilgang)
    }

    @Test fun tilgang_til_egen_ansatt_gir_OK_hvis_søker_ikke_er_egen_ansatt() {
        Mockito.`when`(egenAnsattService!!.erEgenAnsatt(ArgumentMatchers.any(String::class.java)))
                .thenReturn(Boolean.FALSE)
        Assertions.assertThat(tilgangsKontrollService!!.sjekkTilgang("123",
                                                                     saksbehandler,
                                                                     personinfoUtenKode6og7())!!.isHarTilgang)
                .isEqualTo(Tilgang().withHarTilgang(true).isHarTilgang)
    }

    @Test fun hvis_kode6_har_ikke_saksbehandler_tilgang() {
        Mockito.`when`(egenAnsattService!!.erEgenAnsatt(ArgumentMatchers.any(String::class.java)))
                .thenReturn(Boolean.FALSE)
        Assertions.assertThat(tilgangsKontrollService!!.sjekkTilgang("123",
                                                                     saksbehandler,
                                                                     personinfoMedKode6())!!.isHarTilgang)
                .isEqualTo(Tilgang().withHarTilgang(false).isHarTilgang)
    }

    @Test fun hvis_kode7_har_ikke_saksbehandler_tilgang() {
        Mockito.`when`(egenAnsattService!!.erEgenAnsatt(ArgumentMatchers.any(String::class.java)))
                .thenReturn(Boolean.FALSE)
        Assertions.assertThat(tilgangsKontrollService!!.sjekkTilgang("123",
                                                                     saksbehandler,
                                                                     personinfoMedKode7())!!.isHarTilgang)
                .isEqualTo(Tilgang().withHarTilgang(false).isHarTilgang)
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
                .medDiskresjonsKode(TilgangsKontrollService.DISKRESJONSKODE_KODE6)
                .build()
    }

    private fun personinfoMedKode7(): Personinfo {
        return Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                .medFødselsdato(LocalDate.now())
                .medNavn(NAVN)
                .medDiskresjonsKode(TilgangsKontrollService.DISKRESJONSKODE_KODE7)
                .build()
    }

    companion object {
        private const val NAVN = "Navn Navnesen"
        private const val FNR = "fnr"
        private val PERSON_IDENT =
                PersonIdent(FNR)
    }
}
