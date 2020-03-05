package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class PdlHentPersonResponse (val data: PdlPerson?,
                                  val errors: Array<PdlError>?) {
    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }
}

data class PdlPerson (val person: PdlPersonData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonData (
        val foedsel: Array<PdlFødselsDato>,
        val navn: Array<PdlNavn>,
        val kjoenn: Array<PdlKjoenn>,
        val familierelasjoner: Array<Familierelasjon>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato (val foedselsdato: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError (val message: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlNavn(
        val fornavn: String,
        val mellomnavn: String? = null,
        val etternavn: String
) {
    fun fulltNavn(): String {
        return when (mellomnavn) {
            null -> "$fornavn $etternavn"
            else -> "$fornavn $mellomnavn $etternavn"
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlKjoenn(val kjoenn: KJØNN)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Familierelasjon(
        val relatertPersonsIdent: String,
        val relatertPersonsRolle: FAMILIERELASJONSROLLE)

enum class KJØNN {MANN, KVINNE, UKJENT}

enum class FAMILIERELASJONSROLLE { BARN, FAR, MEDMOR, MOR}