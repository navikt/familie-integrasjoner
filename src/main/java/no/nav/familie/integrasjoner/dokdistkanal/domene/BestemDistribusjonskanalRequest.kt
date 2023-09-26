package no.nav.familie.integrasjoner.dokdistkanal.domene

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.kontrakter.felles.Tema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BestemDistribusjonskanalRequest(
    val brukerId: String,
    val mottakerId: String,
    val tema: Tema,
    val dokumenttypeId: String? = null,
    val erArkivert: Boolean? = null,
    @JsonProperty("forsendelseStoerrelse")
    val forsendelseStørrelse: Int? = null,
)
