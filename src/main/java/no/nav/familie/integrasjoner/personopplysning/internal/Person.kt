package no.nav.familie.integrasjoner.personopplysning.internal

data class Person (
        val fødselsdato: String,
        val navn: String,
        val kjønn: String,
        val familierelasjoner: Set<Familierelasjon>)

data class Familierelasjon (
        val personIdent: String,
        val relasjonsrolle: String
)