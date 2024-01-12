package no.nav.familie.integrasjoner.personopplysning.internal

data class PostadresseResponse(
    val navn: String,
    val adresse: Adresse,
)

data class Adresse(
    val adresseKilde: AdresseKildeCode,
    val type: PostadresseType,
    val adresselinje1: String,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val postnummer: String,
    val poststed: String,
    val landkode: String,
    val land: String,
)
enum class AdresseKildeCode {
    BOSTEDSADRESSE,
    OPPHOLDSADRESSE,
    KONTAKTADRESSE,
    DELTBOSTED,
    KONTAKTINFORMASJONFORDØDSBO,
    ENHETPOSTADRESSE,
    ENHETFORRETNINGSADRESSE,
}

enum class PostadresseType {
    NORSKPOSTADRESSE,
    UTENLANDSKPOSTADRESSE;
}
