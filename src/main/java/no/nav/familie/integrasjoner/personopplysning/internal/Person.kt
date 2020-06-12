package no.nav.familie.integrasjoner.personopplysning.internal

import no.nav.familie.kontrakter.felles.personinfo.Bostedsadresse
import no.nav.familie.kontrakter.felles.personinfo.SIVILSTAND

data class Person(
        val fødselsdato: String,
        val navn: String,
        val kjønn: String,
        val familierelasjoner: Set<Familierelasjon>,
        val adressebeskyttelseGradering: ADRESSEBESKYTTELSEGRADERING?,
        val bostedsadresse: Bostedsadresse? = null,
<<<<<<< HEAD
        val sivilstand: SIVILSTAND
=======
        val sivilstand: SIVILSTANDTYPE? = null
>>>>>>> master
)

data class Familierelasjon(
        val personIdent: Personident,
        val relasjonsrolle: String
)

data class Personident(
        val id: String
)



