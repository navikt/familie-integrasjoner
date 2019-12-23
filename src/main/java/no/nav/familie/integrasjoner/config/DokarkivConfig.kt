package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.dokarkiv.metadata.AbstractDokumentMetadata
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class DokarkivConfig {

    @Bean
    fun metadataMap(vararg dokumentMetadata: AbstractDokumentMetadata): Map<String, AbstractDokumentMetadata> {
        return dokumentMetadata.associateBy { it.dokumentTypeId }
    }

}