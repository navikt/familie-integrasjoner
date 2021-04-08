package no.nav.familie.integrasjoner.personopplysning.internal

data class PersonMedRelasjoner(
        val personIdent: String,
        val adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING?,
        val sivilstand: List<PersonMedAdresseBeskyttelse>,
        val fullmakt: List<PersonMedAdresseBeskyttelse>,
        val barn: List<PersonMedAdresseBeskyttelse>,
        val barnsForeldrer: List<PersonMedAdresseBeskyttelse>)

data class PersonMedAdresseBeskyttelse(val personIdent: String,
                                       val adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING?)