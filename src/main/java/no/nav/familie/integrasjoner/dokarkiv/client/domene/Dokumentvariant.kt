package no.nav.familie.integrasjoner.dokarkiv.client.domene

class Dokumentvariant(
    val filtype: String,
    val variantformat: String,
    val fysiskDokument: ByteArray,
    val filnavn: String?,
)
