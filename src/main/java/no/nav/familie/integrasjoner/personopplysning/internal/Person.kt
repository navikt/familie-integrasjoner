package no.nav.familie.integrasjoner.personopplysning.internal

import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND

data class Person(
        val fødselsdato: String,
        val navn: String,
        val kjønn: String,
        val familierelasjoner: Set<Familierelasjon>,
        val adressebeskyttelseGradering: ADRESSEBESKYTTELSEGRADERING?,
        val bostedsadresse: Bostedsadresse? = null,
        val sivilstand: SIVILSTAND?
)

data class Familierelasjon(
        val personIdent: Personident,
        val relasjonsrolle: String
)

data class Personident(
        val id: String
)



