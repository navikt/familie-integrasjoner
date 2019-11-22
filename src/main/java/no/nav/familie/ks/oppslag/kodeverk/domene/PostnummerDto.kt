package no.nav.familie.ks.oppslag.kodeverk.domene

import com.fasterxml.jackson.annotation.JsonProperty

data class PostnummerDto(@JsonProperty("betydninger") val betydninger: Map<String,List<BetydningerDto>>)

data class BetydningerDto(@JsonProperty("gyldigFra") val gyldigFra: String,
                          @JsonProperty("gyldigTil") val gyldigTil: String,
                          @JsonProperty("beskrivelser") val beskrivelser: Map<String,PoststedDto>)

data class PoststedDto(@JsonProperty("term") val term: String,
                       @JsonProperty("tekst") val tekst : String)
