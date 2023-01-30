package no.nav.familie.integrasjoner.dokdist.domene

data class AdresseTo(
    val adressetype: String,
    val postnummer: String?,
    val poststed: String?,
    val adresselinje1: String?,
    val adresselinje2: String?,
    val adresselinje3: String?,
    val land: String,
)
