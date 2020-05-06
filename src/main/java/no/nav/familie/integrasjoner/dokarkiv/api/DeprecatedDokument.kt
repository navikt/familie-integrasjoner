package no.nav.familie.integrasjoner.dokarkiv.api

import javax.validation.constraints.NotEmpty

@Deprecated("Bruk Dokument fra kontrakt")
class DeprecatedDokument(@field:NotEmpty val dokument: ByteArray,
                         @field:NotEmpty val filType: DeprecatedFilType,
                         val filnavn: String?,
                         val tittel: String?,
                         @field:NotEmpty val dokumentType: String)
