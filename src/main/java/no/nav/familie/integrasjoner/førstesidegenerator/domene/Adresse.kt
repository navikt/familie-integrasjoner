package no.nav.familie.integrasjoner.førstesidegenerator.domene

data class Adresse(val adresselinje1: String? = null,
                   val adresselinje2: String? = null,
                   val adresselinje3: String? = null,
                   val postnummer: String? = null,
                   val poststed: String? = null)