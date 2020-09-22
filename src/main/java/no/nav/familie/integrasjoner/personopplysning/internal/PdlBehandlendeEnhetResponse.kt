package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PdlBehandlendeEnhetForPersonResponse(val data: PdlBehandlendeEnhetForPerson,
                                                val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }
}

data class PdlBehandlendeEnhetForPerson(val person: PdlPdlBehandlendeEnhetForPersonData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPdlBehandlendeEnhetForPersonData(
        val adressebeskyttelse: List<Adressebeskyttelse>,
        val geografiskTilknytning: GeografiskTilknytning?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeografiskTilknytning(
        val gtKommune: String?
)

enum class GtType {
    KOMMUNE,
    BYDEL,
    UTLAND,
    UDEFINERT
}