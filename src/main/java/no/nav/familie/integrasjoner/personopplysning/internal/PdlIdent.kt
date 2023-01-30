package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlIdentRequest(
    val variables: PdlIdentRequestVariables,
    val query: String,
)

data class PdlIdentRequestVariables(
    val ident: String,
    val gruppe: String,
    val historikk: Boolean = false,
)

data class PdlIdent(val ident: String, val historisk: Boolean)

data class PdlIdenter(val identer: List<PdlIdent>)

data class PdlHentIdenter(val hentIdenter: PdlIdenter?)
