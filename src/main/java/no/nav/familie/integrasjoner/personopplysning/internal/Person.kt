package no.nav.familie.integrasjoner.personopplysning.internal

data class Person(
        val fødselsdato: String,
        val navn: String,
        val kjønn: String,
        val familierelasjoner: Set<Familierelasjon>,
        val adressebeskyttelseGradering: ADRESSEBESKYTTELSEGRADERING?)

data class Familierelasjon(
        val personIdent: Personident,
        val relasjonsrolle: String
)

data class Personident(
        val id: String
)