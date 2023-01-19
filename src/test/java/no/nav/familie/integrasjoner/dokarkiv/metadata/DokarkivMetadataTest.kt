package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DokarkivMetadataTest {

    @Test
    fun `skal gi riktig metadata for Dokumenttype`() {
        Dokumenttype.values().forEach {
            Assertions.assertEquals(
                it,
                it.tilDokumentmetadata().dokumenttype
            )
        }
    }

    @Test
    fun `Skal være like mange Dokumentmetadata som det er Dokumenttyper`() {
        val dokumentMetadataObjekter = Dokumentmetadata::class.sealedSubclasses
            .filter { it.isFinal }

        Assertions.assertEquals(
            Dokumenttype.values().size,
            dokumentMetadataObjekter.size
        )
    }

    @Test
    internal fun `brevkode må være under 50 tegn`() {
        val tittelOver50Tegn = Dokumentmetadata::class.sealedSubclasses
            .mapNotNull { it.objectInstance }
            .filter { (it.brevkode?.length ?: 0) > 50 }

        assertThat(tittelOver50Tegn).isEmpty()
    }
}
