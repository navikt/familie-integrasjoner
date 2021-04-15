package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlPersonBolkRequest(val variables: PdlPersonBolkRequestVariables,
                                val query: String)
data class PdlPersonBolkRequestVariables(val identer: List<String>)
