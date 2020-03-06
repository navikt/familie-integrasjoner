package no.nav.familie.integrasjoner.journalpost.domene

data class Journalpost(val journalpostId: String,
                       val journalposttype: Journalposttype,
                       val journalstatus: Journalstatus,
                       val tema: String?,
                       val behandlingstema: String?,
                       val sak: Sak?,
                       val bruker: Bruker?,
                       val journalforendeEnhet: String?,
                       val kanal: String?,
                       val dokumenter: List<DokumentInfo>?)

data class Sak(val arkivsaksnummer: String?,
               var arkivsaksystem: String?,
               val fagsakId: String?,
               val fagsaksystem: String?)

data class Bruker(val id: String,
                  val type: BrukerIdType)

data class DokumentInfo(val tittel: String?,
                        val brevkode: String?,
                        val dokumentstatus: Dokumentstatus?,
                        val dokumentvarianter: List<Dokumentvariant>?)

data class Dokumentvariant(val variantformat: String)

enum class Journalposttype {
    I,
    U,
    N
}

enum class Journalstatus {
    MOTTATT,
    JOURNALFOERT,
    FERDIGSTILT,
    EKSPEDERT,
    UNDER_ARBEID,
    FEILREGISTRERT,
    UTGAAR,
    AVBRUTT,
    UKJENT_BRUKER,
    RESERVERT,
    OPPLASTING_DOKUMENT,
    UKJENT
}

enum class Dokumentstatus {
    FERDIGSTILT,
    AVBRUTT,
    UNDER_REDIGERING,
    KASSERT
}

enum class BrukerIdType {
    AKTOERID,
    FNR,
    ORGNR
}