package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlPersonRequest (val variablesPerson: PdlPersonRequestVariables,
                             val query: String)