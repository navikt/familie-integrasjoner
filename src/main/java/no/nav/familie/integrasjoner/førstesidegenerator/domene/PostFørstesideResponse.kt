package no.nav.familie.integrasjoner.førstesidegenerator.domene

import com.fasterxml.jackson.annotation.JsonProperty

class PostFørstesideResponse(
    @JsonProperty("foersteside")
    val førsteside: ByteArray,
    @JsonProperty("loepenummer")
    val løpenummer: String? = null
)
