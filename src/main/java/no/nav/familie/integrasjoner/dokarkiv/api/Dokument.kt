package no.nav.familie.integrasjoner.dokarkiv.api

import javax.validation.constraints.NotEmpty

class Dokument(@field:NotEmpty val dokument: ByteArray,
               @field:NotEmpty val filType: FilType,
               val filnavn: String?,
//               val dokumenttittel: String?,
               @field:NotEmpty val dokumentType: String)
