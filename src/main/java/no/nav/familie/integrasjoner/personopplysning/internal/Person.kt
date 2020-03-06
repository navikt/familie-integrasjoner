package no.nav.familie.integrasjoner.personopplysning.internal

data class Person (
        val fødselsdato: String,
        val navn: String,
        val kjønn: String,
        val familierelasjoner: Array<Familierelasjon>)

data class Familierelasjon (
        val ident: String,
        val rolle: String
)