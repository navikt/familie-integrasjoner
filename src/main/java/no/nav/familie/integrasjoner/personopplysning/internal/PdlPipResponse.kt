package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


data class PipPersondataResponseList(
    val personer: Map<String, PipPersonDataResponse>,
)
data class PipPersonDataResponse(
    val person: PipPerson?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PipPerson(
    val adressebeskyttelse: List<PipAdressebeskyttelse>,
    val familierelasjoner: List<PipFamilierelasjoner>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PipAdressebeskyttelse(
    val gradering: ADRESSEBESKYTTELSEGRADERING,
)

data class PipFamilierelasjoner(
    val relatertPersonIdent: String
)
