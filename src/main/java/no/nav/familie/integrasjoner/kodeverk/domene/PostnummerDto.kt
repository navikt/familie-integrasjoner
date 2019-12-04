package no.nav.familie.integrasjoner.kodeverk.domene

data class PostnummerDto(val betydninger: Map<String, List<BetydningerDto>>)

data class BetydningerDto(val gyldigFra: String,
                          val gyldigTil: String,
                          val beskrivelser: Map<String, PoststedDto>)

data class PoststedDto(val term: String,
                       val tekst: String)
