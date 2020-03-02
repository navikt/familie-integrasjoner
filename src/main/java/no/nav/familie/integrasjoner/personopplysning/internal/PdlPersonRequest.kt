package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlPersonRequest (val variables: PdlRequestVariable,
                             val query: String)