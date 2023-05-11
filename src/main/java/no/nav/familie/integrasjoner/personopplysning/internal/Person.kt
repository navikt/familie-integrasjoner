package no.nav.familie.integrasjoner.personopplysning.internal

data class Person(
    val navn: String,
    val adressebeskyttelseGradering: ADRESSEBESKYTTELSEGRADERING?,
)

data class Familierelasjon(
    val personIdent: Personident,
    val relasjonsrolle: String,
)

data class Personident(
    val id: String,
)
