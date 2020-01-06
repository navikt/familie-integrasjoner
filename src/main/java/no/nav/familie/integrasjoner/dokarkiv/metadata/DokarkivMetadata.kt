package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import org.springframework.stereotype.Component

@Component
class DokarkivMetadata(vararg dokumentMetadata: DokumentMetadata) {

    val metadata: Map<String, DokumentMetadata> = dokumentMetadata.associateBy { it.dokumentTypeId }

    fun getMetadata(dokument: Dokument): DokumentMetadata {
        return metadata[dokument.dokumentType] ?: error("Ukjent dokumenttype ${dokument.dokumentType}")
    }
}