package no.nav.familie.integrasjoner.kodeverk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkSpråk
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class KodeverkServiceTest {

    private val kodeverkClientMock: KodeverkClient = mockk()
    private val kodeverkService = KodeverkService(kodeverkClientMock)

    @Test
    fun name() {
        val kodeverkDto =
                objectMapper.readValue<KodeverkDto>((KodeverkServiceTest::class as Any).javaClass.classLoader.getResourceAsStream(
                        "kodeverk/kodeverk.json"))
        every { kodeverkClientMock.hentPostnummer() } returns kodeverkDto
        val hentPoststed = kodeverkService.hentPoststed("0575")
        println(hentPoststed)
    }

    @Test
    fun `skal returnere poststed`() {
        val beskrivelse = BeskrivelseDto(POSTSTED, "")
        val beytning = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelse))
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
        val betydning = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelse))
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
