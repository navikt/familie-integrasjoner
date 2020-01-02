package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.api.Dokument
import org.springframework.stereotype.Component

@Component
class DokarkivMetadata(vararg dokumentMetadata: AbstractDokumentMetadata) {

    val metadata: Map<String, AbstractDokumentMetadata> = dokumentMetadata.associateBy { it.dokumentTypeId }

    fun getMetadata(dokument: Dokument): AbstractDokumentMetadata {
        return metadata[dokument.dokumentType] ?: error("Ukjent dokumenttype ${dokument.dokumentType}")
    }
}