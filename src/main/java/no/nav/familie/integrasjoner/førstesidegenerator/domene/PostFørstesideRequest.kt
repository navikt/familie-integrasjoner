package no.nav.familie.integrasjoner.førstesidegenerator.domene

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.familie.kontrakter.felles.Språkkode

data class PostFørstesideRequest(
    @JsonProperty("spraakkode")
    val språkkode: Språkkode = Språkkode.NB,
    val adresse: Adresse? = null,
    val netsPostboks: String? = null,
    val avsender: Avsender? = null,
    val bruker: Bruker? = null,
    val ukjentBrukerPersoninfo: String? = null,
    val tema: String? = null,
    val behandlingstema: String? = null,
    val arkivtittel: String? = null,
    val vedleggsliste: List<String> = ArrayList(),
    val navSkjemaId: String? = null,
    val overskriftstittel: String? = null,
    @JsonProperty("dokumentlisteFoersteside")
    val dokumentlisteFørsteside: List<String> = ArrayList(),
    @JsonProperty("foerstesidetype")
    val førstesidetype: Førstesidetype? = null,
    val enhetsnummer: String? = null,
    val arkivsak: Arkivsak? = null,
)
