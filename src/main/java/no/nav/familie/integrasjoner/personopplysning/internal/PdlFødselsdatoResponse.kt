package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class PdlFødselsdatoResponse (val data: PdlPersonData?,
                                   val errors: Array<PdlError>?) {
    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}

data class PdlPersonData (val person: PdlFødslerData)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødslerData (val foedsel: Array<PdlFødselsDato>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato (
        val foedselsdato: String
) {
    @JsonProperty
    fun getFødselsdato() = foedselsdato
}

data class PdlError (val message: String)