package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlRequest (val variables: PdlRequestVariables,
                       val query: String)