package no.nav.familie.integrasjoner.sak

import io.swagger.annotations.ApiModelProperty

data class Skyggesak(
        val tema: String,
        val applikasjon: String,
        val aktoerId: String,
        val fagsakNr: String,
        @ApiModelProperty(readOnly = true) val id: Long? = null,
)