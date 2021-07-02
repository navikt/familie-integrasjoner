package no.nav.familie.integrasjoner.kodeverk

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkSpråk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.annotation.Cacheable
import java.lang.reflect.Modifier
import java.time.LocalDate
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.javaMethod

class CachedKodeverkServiceTest {

    private val kodeverkClientMock: KodeverkClient = mockk()
    private val kodeverkService = CachedKodeverkService(kodeverkClientMock)

    @Test
    fun `skal returnere poststed`() {
        val beskrivelse = BeskrivelseDto(POSTSTED, "")
        val beytning = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelse))
        val kodeverk = KodeverkDto(mapOf(POSTNUMMER to listOf(beytning)))

        every { kodeverkClientMock.hentPostnummer() } returns kodeverk

        val poststedTest = kodeverkService.hentPostnummer()[POSTNUMMER]
        assertThat(poststedTest).isEqualTo(POSTSTED)
    }

    @Test
    fun `skal returnere tom poststed hvis den ikke finnes`() {
        every { kodeverkClientMock.hentPostnummer() } returns KodeverkDto(emptyMap())

        val poststedTest = kodeverkService.hentPostnummer()[POSTNUMMER]
        assertThat(poststedTest).isNull()
    }

    @Test
    fun `skal returnere landkod`() {
        val beskrivelse = BeskrivelseDto(LAND, "")
        val betydning = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelse))
        val kodeverk = KodeverkDto(mapOf(LANDKODE to listOf(betydning)))

        every { kodeverkClientMock.hentLandkoder() } returns kodeverk

        val land = kodeverkService.hentLandkoder()[LANDKODE]
        assertThat(land).isEqualTo(LAND)
    }

    @Test
    fun `skal returnere tom land hvis den ikke finnes`() {
        every { kodeverkClientMock.hentLandkoder() } returns KodeverkDto(emptyMap())

        val land = kodeverkService.hentLandkoder()[LANDKODE]
        assertThat(land).isNull()
    }

    @Test
    fun `alle public metoder skal være annotert med @Cacheable`() {
        val publikMetoderUtenCacheable = CachedKodeverkService::class.declaredMemberFunctions
                .filter { Modifier.isPublic(it.javaMethod!!.modifiers) }
                .filter { it.annotations.none { it.annotationClass == Cacheable::class } }
        assertThat(publikMetoderUtenCacheable).isEmpty()
    }

    companion object {

        private const val POSTNUMMER = "0557"
        private const val POSTSTED = "Oslo"
        private const val LANDKODE = "NOR"
        private const val LAND = "Norge"
    }
}
