package no.nav.familie.integrasjoner.kodeverk.domene

import com.fasterxml.jackson.annotation.JsonProperty

data class KodeverkDto(@JsonProperty("betydninger") val betydninger: Map<String, List<BetydningDto>>)

data class BetydningDto(@JsonProperty("gyldigFra") val gyldigFra: String,
                        @JsonProperty("gyldigTil") val gyldigTil: String,
                        @JsonProperty("beskrivelser") val beskrivelser: Map<String, BeskrivelseDto>)

data class BeskrivelseDto(@JsonProperty("term") val term: String,
                          @JsonProperty("tekst") val tekst: String)

fun KodeverkDto.mapTerm() = betydninger.mapValues { it.value[0].beskrivelser[Språk.BOKMÅL.kode]?.term ?: "" }