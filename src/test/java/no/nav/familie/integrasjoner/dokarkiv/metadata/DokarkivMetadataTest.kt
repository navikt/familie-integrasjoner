package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

internal class DokarkivMetadataTest {
    @Test
    internal fun `brevkode må være under 50 tegn`() {
        val tittelOver50Tegn = hentAlleDokumentMedadataKlasser().filter { (it.brevkode?.length ?: 0) > 50 }
        assertThat(tittelOver50Tegn).isEmpty()
    }

    @Test
    fun `Dokumenttypen i dokumentmetadata mapper tilbake til samme dokumentmetadata`() {
        hentAlleDokumentMedadataKlasser().forEach {
            assertTrue(it.dokumenttype.tilMetadata() == it)
        }
    }

    @Test
    fun `Alle dokumettyper mapper til medtadata med samme dokumenttype i parameterene`() {
        Dokumenttype.values().forEach {
            assertTrue(it.tilMetadata().dokumenttype == it)
        }
    }

    private fun hentAlleDokumentMedadataKlasser() =
        Dokumentmetadata::class
            .sealedSubclasses
            .hentNøstedeKlasser()
            .mapNotNull { it.objectInstance }

    private fun <T : Any> List<KClass<out T>>.hentNøstedeKlasser(): List<KClass<out T>> =
        flatMap {
            if (it.isSealed) {
                it.sealedSubclasses.hentNøstedeKlasser()
            } else {
                listOf(it)
            }
        }
}
