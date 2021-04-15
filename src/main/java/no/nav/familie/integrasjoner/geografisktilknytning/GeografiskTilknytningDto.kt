package no.nav.familie.integrasjoner.geografisktilknytning

data class PdlHentGeografiskTilknytning(val hentGeografiskTilknytning: GeografiskTilknytningDto?)

data class GeografiskTilknytningDto(val gtType: GeografiskTilknytningType,
                                    val gtKommune: String?,
                                    val gtBydel: String?,
                                    val gtLand: String?)


enum class GeografiskTilknytningType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}

data class PdlGeografiskTilknytningRequest(val variables: PdlGeografiskTilknytningVariables,
                                           val query: String)

data class PdlGeografiskTilknytningVariables(val ident: String)