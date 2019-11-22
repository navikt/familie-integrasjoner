package no.nav.familie.ef.mottak.api.kodeverk.domene

data class PostnummerDto(val betydninger: Map<String,List<BetydningerDto>>)

data class BetydningerDto(val gyldigFra: String,
                          val gyldigTil: String,
                          val beskrivelser: Map<String,PoststedDto>)

data class PoststedDto(val term: String,
                       val tekst:String)

