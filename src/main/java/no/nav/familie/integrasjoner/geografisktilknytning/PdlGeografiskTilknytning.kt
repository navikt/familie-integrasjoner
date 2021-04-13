package no.nav.familie.integrasjoner.geografisktilknytning

data class PdlHentGeografiskTilknytning(val hentGeografiskTilknytning: PdlGeografiskTilknytning)

data class PdlGeografiskTilknytning(val gtType: GeografiskTilknytningType,
                                    val gtKommune: String?,
                                    val gtBydel: String?,
                                    val gtLand: String?){

    fun hentGeografiskTilknytning(): String {
        return when (gtType) {
            GeografiskTilknytningType.KOMMUNE -> gtKommune!!
            GeografiskTilknytningType.BYDEL -> gtBydel!!
            GeografiskTilknytningType.UTLAND -> gtLand!!
            GeografiskTilknytningType.UDEFINERT -> "ingen geografisk tilknytning"
            null -> "fant ingen geografisktilknytning"
        }
    }
}


enum class GeografiskTilknytningType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}

data class PdlGeografiskTilknytningRequest(val variables: PdlGeografiskTilknytningVariables,
                                           val query: String)

data class PdlGeografiskTilknytningVariables(val ident: String)