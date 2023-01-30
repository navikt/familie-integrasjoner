package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlPersonRequest(
    val variables: PdlPersonRequestVariables,
    val query: String,
)
