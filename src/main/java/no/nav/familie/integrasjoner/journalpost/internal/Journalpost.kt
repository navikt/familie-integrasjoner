package no.nav.familie.integrasjoner.journalpost.internal

data class Journalpost(val journalpostId: String,
                       val journalpostype: Journalposttype?,
                       val journalstatus: Journalstatus?,
                       val tema: Tema?,
                       val behandlingstema: String?,
                       val sak: Sak? = null,
                       val journalforendeEnhet: String?,
                       val kanal: Kanal?,
                       val dokumenter: DokumentInfo?)

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

enum class Tema {
    BAR,
    ENF,
    KON
}

data class Sak(val arkivsaksnummer: String? = null,
               var arkivsaksystem: String? = null,
               val fagsakId: String? = null,
               val fagsaksystem: String? = null)

enum class Kanal {
    ALTINN,
    EIA,
    NAV_NO,
    NAV_NO_UINNLOGGET,
    SKAN_NETS,
    SKAN_PEN,
    EESSI,
    EKST_OPPS,
    SENTRAL_UTSKRIFT,
    LOKAL_UTSKRIFT,
    SDP,
    TRYGDERETTEN,
    HELSENETTET,
    INGEN_DISTRIBUSJON,
    UKJENT
}

data class DokumentInfo(val tittel: String?,
                        val brevkode: String?,
                        val dokumentstatus: Dokumentstatus?,
                        val dokumentvarianter: Dokumentvariant?)

enum class Dokumentstatus {
    FERDIGSTILT,
    AVBRUTT,
    UNDER_REDIGERING,
    KASSERT
}

data class Dokumentvariant(val variantformat: Variantformat?)

enum class Variantformat {
    ARKIV,
    FULLVERSJON,
    PRODUKSJON,
    PRODUKSJON_DLF,
    SLADDET,
    ORIGINAL
}