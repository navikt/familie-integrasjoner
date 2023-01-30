package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.Tema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ArbeidsfordelingClientTest {

    private val TEMA = Tema.ENF
    private val GEOGRAFISK_TILKNYTNING = "GEOGRAFISK_TILKNYTNING"
    private val DISKRESJONSKODE = "DISKRESJONSKODE"
    private val ENHET_ID = "ENHET_ID"

    @MockK
    lateinit var service: ArbeidsfordelingV1

    @InjectMockKs
    lateinit var client: ArbeidsfordelingClient

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `skal returnere enhet ved søk på tema`() {
        val requestSlot = slot<FinnBehandlendeEnhetListeRequest>()

        every {
            service.finnBehandlendeEnhetListe(capture(requestSlot))
        } returns FinnBehandlendeEnhetListeResponse().apply {
            behandlendeEnhetListe.apply {
                add(
                    Organisasjonsenhet().apply {
                        enhetId = ENHET_ID
                        enhetNavn = "Test"
                    },
                )
            }
        }

        val response = client.finnBehandlendeEnhet(TEMA, null, null)
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.tema.value).isEqualTo(TEMA.toString())
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.behandlingstema).isNull()
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.geografiskTilknytning).isNull()
        assertThat(response.first().enhetId).isEqualTo(ENHET_ID)
    }

    @Test
    fun `skal returenere enhet ved søk på tema, geografisk tilknytning og diskresjonskode`() {
        val requestSlot = slot<FinnBehandlendeEnhetListeRequest>()

        every {
            service.finnBehandlendeEnhetListe(capture(requestSlot))
        } returns FinnBehandlendeEnhetListeResponse().apply {
            behandlendeEnhetListe.apply {
                add(
                    Organisasjonsenhet().apply {
                        enhetId = ENHET_ID
                        enhetNavn = "Test"
                    },
                )
            }
        }

        val response = client.finnBehandlendeEnhet(TEMA, GEOGRAFISK_TILKNYTNING, DISKRESJONSKODE)
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.tema.value).isEqualTo(TEMA.toString())
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.diskresjonskode.value).isEqualTo(DISKRESJONSKODE)
        assertThat(requestSlot.captured.arbeidsfordelingKriterier.geografiskTilknytning.value).isEqualTo(GEOGRAFISK_TILKNYTNING)
        assertThat(response.first().enhetId).isEqualTo(ENHET_ID)
    }

    @Test
    fun `skal kaste oppslagexception ved ugyldig input`() {
        val requestSlot = slot<FinnBehandlendeEnhetListeRequest>()

        every {
            service.finnBehandlendeEnhetListe(capture(requestSlot))
        } throws FinnBehandlendeEnhetListeUgyldigInput("Ugyldig input", null)

        val e = assertThrows<OppslagException> { client.finnBehandlendeEnhet(TEMA, null, null) }
        assertThat(e.message).isEqualTo("Ugyldig input tema=ENF geografiskOmråde=null melding=Ugyldig input")
    }
}
