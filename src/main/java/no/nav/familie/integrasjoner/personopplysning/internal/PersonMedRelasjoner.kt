package no.nav.familie.integrasjoner.personopplysning.internal

import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND

data class PersonMedRelasjoner(
        val personIdent: String,
        val adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING?,
        val sivilstand: List<PersonMedAdresseBeskyttelse>,
        val fullmakt: List<PersonMedAdresseBeskyttelse>,
        val barn: List<PersonMedAdresseBeskyttelse>,
        val barnsForeldrer: List<PersonMedAdresseBeskyttelse>)

data class PersonMedAdresseBeskyttelse(val personIdent: String,
                                       val adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING?)

fun List<PersonMedAdresseBeskyttelse>.personIdentMedKode6(): String? =
        this.find { it.adressebeskyttelse == STRENGT_FORTROLIG || it.adressebeskyttelse == STRENGT_FORTROLIG_UTLAND }
                ?.personIdent

fun List<PersonMedAdresseBeskyttelse>.personMedKode7(): String? =
        this.find { it.adressebeskyttelse == FORTROLIG }?.personIdent
