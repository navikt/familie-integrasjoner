package no.nav.familie.integrasjoner.personopplysning.internal

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTANDTYPE

data class PdlResponse<T>(
    val data: T,
    val errors: List<PdlError>?,
    val extensions: PdlExtensions?,
) {
    fun harFeil(): Boolean = errors != null && errors.isNotEmpty()

    fun harAdvarsel(): Boolean = !extensions?.warnings.isNullOrEmpty()

    fun errorMessages(): String = errors?.joinToString { it -> it.message } ?: ""

    fun harNotFoundFeil(): Boolean = errors?.any { it.extensions?.notFound() == true } ?: false

    fun harUnauthorizedFeil(): Boolean = errors?.any { it.extensions?.unauthorized() == true } ?: false

    fun tilPdlUnauthorizedDetails(): List<PdlUnauthorizedDetails> = errors?.filter { it.extensions?.unauthorized() == true }?.mapNotNull { it.extensions?.details?.let { details -> jsonMapper.convertValue(details, PdlUnauthorizedDetails::class.java) } } ?: emptyList()
}

data class PersonDataBolk<T>(
    val ident: String,
    val code: String,
    val person: T?,
)

data class PersonBolk<T>(
    val personBolk: List<PersonDataBolk<T>>,
)

data class PdlBolkResponse<T>(
    val data: PersonBolk<T>?,
    val errors: List<PdlError>?,
    val extensions: PdlExtensions?,
) {
    fun errorMessages(): String = errors?.joinToString { it -> it.message } ?: ""

    fun harAdvarsel(): Boolean = !extensions?.warnings.isNullOrEmpty()
}

data class PdlPersonMedAdressebeskyttelse(
    val person: PdlAdressebeskyttelse?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlAdressebeskyttelse(
    val adressebeskyttelse: List<Adressebeskyttelse>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonMedRelasjonerOgAdressebeskyttelse(
    val forelderBarnRelasjon: List<PdlForelderBarnRelasjon>,
    val sivilstand: List<Sivilstand>,
    val adressebeskyttelse: List<Adressebeskyttelse>,
)

data class PdlPerson(
    val person: PdlPersonData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlPersonData(
    val navn: List<PdlNavn>,
    val adressebeskyttelse: List<Adressebeskyttelse>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlError(
    val message: String,
    val extensions: PdlErrorExtensions?,
)

data class PdlErrorExtensions(
    val code: String?,
    val details: Any?,
) {
    fun notFound() = code == "not_found"

    fun unauthorized() = code == "unauthorized"
}

data class PdlUnauthorizedDetails(
    val type: String?,
    val cause: String?,
    val policy: String,
)

data class PdlExtensions(
    val warnings: List<PdlWarning>?,
)

data class PdlWarning(
    val details: Any?,
    val id: String?,
    val message: String?,
    val query: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlNavn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
) {
    fun fulltNavn(): String =
        when (mellomnavn) {
            null -> "$fornavn $etternavn"
            else -> "$fornavn $mellomnavn $etternavn"
        }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlForelderBarnRelasjon(
    val relatertPersonsIdent: String?,
    val relatertPersonsRolle: FORELDERBARNRELASJONROLLE,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Adressebeskyttelse(
    val gradering: ADRESSEBESKYTTELSEGRADERING,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sivilstand(
    val type: SIVILSTANDTYPE,
    val relatertVedSivilstand: String?,
)

enum class KJØNN {
    MANN,
    KVINNE,
    UKJENT,
}

enum class FORELDERBARNRELASJONROLLE {
    BARN,
    FAR,
    MEDMOR,
    MOR,
}

enum class ADRESSEBESKYTTELSEGRADERING(
    val diskresjonskode: String?,
) {
    STRENGT_FORTROLIG_UTLAND("SPSF"), // Kode 19
    FORTROLIG("SPFO"), // Kode 7
    STRENGT_FORTROLIG("SPSF"), // Kode 6
    UGRADERT(null),
}
