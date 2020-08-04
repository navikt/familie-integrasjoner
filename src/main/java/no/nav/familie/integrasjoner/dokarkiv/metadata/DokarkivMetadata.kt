package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.kontrakter.felles.dokarkiv.Dokument
import org.springframework.stereotype.Component

@Component
class DokarkivMetadata(vararg dokumentMetadata: DokumentMetadata) {

    val metadata: Map<String, DokumentMetadata> = dokumentMetadata.associateBy { it.dokumentTypeId }

    fun getMetadata(deprecatedDokument: Dokument): DokumentMetadata {
        return metadata[deprecatedDokument.dokumentType] ?: error("Ukjent dokumenttype ${deprecatedDokument.dokumentType}")
    }
}