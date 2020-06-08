package no.nav.familie.integrasjoner.kodeverk

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.integrasjoner.kodeverk.domene.BetydningDto
import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import no.nav.familie.integrasjoner.kodeverk.domene.BeskrivelseDto
import no.nav.familie.integrasjoner.kodeverk.domene.Språk
import org.assertj.core.api.Assertions.*
import org.junit.Test

class KodeverkServiceTest {

    private val kodeverkClientMock: KodeverkClient = mockk()
    private val kodeverkService = KodeverkService(kodeverkClientMock)

    @Test
    fun `skal returnere poststed`() {
        val beskrivelse = BeskrivelseDto(POSTSTED, "")
        val beytning = BetydningDto("", "", mapOf(Språk.BOKMÅL.kode to beskrivelse))
        val kodeverk = KodeverkDto(mapOf(POSTNUMMER to listOf(beytning)))

        every { kodeverkClientMock.hentPostnummer() } returns kodeverk

        val poststedTest = kodeverkService.hentPoststed(POSTNUMMER)
        assertThat(poststedTest).isEqualTo(POSTSTED)
    }

    @Test
    fun `skal returnere tom poststed hvis den ikke finnes`() {
        every { kodeverkClientMock.hentPostnummer() } returns KodeverkDto(emptyMap())

        val poststedTest = kodeverkService.hentPoststed(POSTNUMMER)
        assertThat(poststedTest).isEmpty()
    }

    @Test
    fun `skal returnere landkod`() {
        val beskrivelse = BeskrivelseDto(LAND, "")
        val betydning = BetydningDto("", "", mapOf(Språk.BOKMÅL.kode to beskrivelse))
        val kodeverk = KodeverkDto(mapOf(LANDKODE to listOf(betydning)))

        every { kodeverkClientMock.hentLandkoder() } returns kodeverk

        val land = kodeverkService.hentLandkode(LANDKODE)
        assertThat(land).isEqualTo(LAND)
    }

    @Test
    fun `skal returnere tom land hvis den ikke finnes`() {
        every { kodeverkClientMock.hentLandkoder() } returns KodeverkDto(emptyMap())

        val land = kodeverkService.hentLandkode(LANDKODE)
        assertThat(land).isEmpty()
    }

    companion object {
        private const val POSTNUMMER = "0557"
        private const val POSTSTED = "Oslo"
        private const val LANDKODE = "NOR"
        private const val LAND = "Norge"
    }
}
