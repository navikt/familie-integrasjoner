package no.nav.familie.integrasjoner.medlemskap

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class MedlemskapsUnntakResponse {
    @get:JsonProperty("dekning") @JsonProperty("dekning") var dekning: String? = null
    @get:JsonProperty("fraOgMed") @JsonProperty("fraOgMed") var fraOgMed: Date? = null
    @get:JsonProperty("grunnlag") @JsonProperty("grunnlag") var grunnlag: String? = null
    @get:JsonProperty("helsedel") @JsonProperty("helsedel") var isHelsedel: Boolean? = null
    @get:JsonProperty("ident") @JsonProperty("ident") var ident: String? = null
    @get:JsonProperty("lovvalg") @JsonProperty("lovvalg") var lovvalg: String? = null
    @get:JsonProperty("lovvalgsland") @JsonProperty("lovvalgsland") var lovvalgsland: String? = null
    @get:JsonProperty("medlem") @JsonProperty("medlem") var isMedlem: Boolean? = null
    @get:JsonProperty("status") @JsonProperty("status") var status: String? = null
    @get:JsonProperty("statusaarsak") @JsonProperty("statusaarsak") var statusaarsak: String? = null
    @get:JsonProperty("tilOgMed") @JsonProperty("tilOgMed") var tilOgMed: Date? = null
    @get:JsonProperty("unntakId") @JsonProperty("unntakId") var unntakId: Long? = null

}