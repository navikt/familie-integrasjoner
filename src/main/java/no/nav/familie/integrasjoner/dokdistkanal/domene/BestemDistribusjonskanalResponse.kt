package no.nav.familie.integrasjoner.dokdistkanal.domene

data class BestemDistribusjonskanalResponse(
    val distribusjonskanal: String,
    val regel: String,
    val regelBegrunnelse: String,
)
