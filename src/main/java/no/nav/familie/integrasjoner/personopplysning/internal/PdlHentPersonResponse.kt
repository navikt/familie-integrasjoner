package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PdlHentPersonResponse (val data: PdlPersonData?,
                                  val errors: Array<PdlError>?) {
    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}

data class PdlPersonData (val person: PdlFødslerData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødslerData (val foedsel: Array<PdlFødselsDato>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato (val foedselsdato: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError (val message: String)