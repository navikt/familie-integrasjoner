package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND

data class PdlResponse<T>(val data: T,
                          val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }

    fun harNotFoundFeil(): Boolean {
        return errors?.any { it.extensions?.notFound() == true } ?: false
    }
}

data class PdlPerson(val person: PdlPersonData?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonData(val foedsel: List<PdlFødselsDato>,
                         val navn: List<PdlNavn>,
                         val kjoenn: List<PdlKjoenn>,
                         val familierelasjoner: List<PdlFamilierelasjon> = emptyList(),
                         val adressebeskyttelse: List<Adressebeskyttelse>,
                         val bostedsadresse: List<Bostedsadresse?>,
                         val sivilstand: List<Sivilstand?>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFødselsDato(val foedselsdato: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError(val message: String,
                    val extensions: PdlExtensions?)

data class PdlExtensions(val code: String) {
    fun notFound() = code == "not_found"
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlNavn(val fornavn: String,
                   val mellomnavn: String? = null,
                   val etternavn: String) {

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
data class PdlFamilierelasjon(val relatertPersonsIdent: String,
                              val relatertPersonsRolle: FAMILIERELASJONSROLLE)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Adressebeskyttelse(
        val gradering: ADRESSEBESKYTTELSEGRADERING
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sivilstand(
        val type: SIVILSTAND
)

enum class KJØNN {
    MANN,
    KVINNE,
    UKJENT
}

enum class FAMILIERELASJONSROLLE {
    BARN,
    FAR,
    MEDMOR,
    MOR
}

enum class ADRESSEBESKYTTELSEGRADERING {
    STRENGT_FORTROLIG_UTLAND, // Kode 19
    FORTROLIG, // Kode 7
    STRENGT_FORTROLIG, // Kode 6
    UGRADERT
}
