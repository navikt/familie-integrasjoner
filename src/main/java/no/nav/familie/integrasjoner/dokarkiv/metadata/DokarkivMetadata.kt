package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.api.DeprecatedDokument
import org.springframework.stereotype.Component

@Component
class DokarkivMetadata(vararg dokumentMetadata: DokumentMetadata) {

    val metadata: Map<String, DokumentMetadata> = dokumentMetadata.associateBy { it.dokumentTypeId }

    fun getMetadata(deprecatedDokument: DeprecatedDokument): DokumentMetadata {
        return metadata[deprecatedDokument.dokumentType] ?: error("Ukjent dokumenttype ${deprecatedDokument.dokumentType}")
    }
}