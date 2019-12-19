package no.nav.familie.integrasjoner.dokarkiv.api

class Dokument(val dokument: ByteArray,
               val filType: FilType,
               val filnavn: String?,
               val dokumentType: DokumentType)
