package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Dokument
import org.springframework.stereotype.Component
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument as DeprecatedDokument

@Component
class DokarkivMetadata(vararg dokumentmetadata: Dokumentmetadata) {

    val metadata: Map<Dokumenttype, Dokumentmetadata> = dokumentmetadata.associateBy { it.dokumenttype }

    fun getMetadata(deprecatedDokument: DeprecatedDokument): Dokumentmetadata {
        return metadata[deprecatedDokument.dokumentType] ?: error("Ukjent dokumenttype ${deprecatedDokument.dokumentType}")
    }

    fun getMetadata(dokument: Dokument): Dokumentmetadata {
        return metadata[dokument.dokumenttype] ?: error("Ukjent dokumenttype ${dokument.dokumenttype}")
    }
}
