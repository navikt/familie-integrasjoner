package no.nav.familie.integrasjoner.journalpost.internal

data class Journalpost(val journalpostId: String,
                       val journalposttype: Journalposttype?,
                       val journalstatus: Journalstatus?,
                       val tema: Tema?,
                       val behandlingstema: String?,
                       val sak: Sak?,
                       val journalforendeEnhet: String?,
                       val kanal: Kanal?,
                       val dokumenter: List<DokumentInfo>?)

data class Sak(val arkivsaksnummer: String?,
               var arkivsaksystem: String?,
               val fagsakId: String?,
               val fagsaksystem: String?)

data class DokumentInfo(val tittel: String?,
                        val brevkode: String?,
                        val dokumentstatus: Dokumentstatus?,
                        val dokumentvarianter: List<Dokumentvariant>?)

data class Dokumentvariant(val variantformat: Variantformat?)

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
    AAR,
    AGR,
    BAR,
    BID,
    BIL,
    DAG,
    ENF,
    ERS,
    FAR,
    FEI,
    FOR,
    FOS,
    FUL,
    GEN,
    GRA,
    GRU,
    HEL,
    HJE,
    IAR,
    IND,
    KON,
    KTR,
    MED,
    MOB,
    OMS,
    OPA,
    OPP,
    PEN,
    PER,
    REH,
    REK,
    RPO,
    RVE,
    SAA,
    SAK,
    SAP,
    SER,
    SIK,
    STO,
    SUP,
    SYK,
    SYM,
    TIL,
    TRK,
    TRY,
    TSO,
    TSR,
    UFM,
    UFO,
    UKJ,
    VEN,
    YRA,
    YRK
}

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

enum class Dokumentstatus {
    FERDIGSTILT,
    AVBRUTT,
    UNDER_REDIGERING,
    KASSERT
}

enum class Variantformat {
    ARKIV,
    FULLVERSJON,
    PRODUKSJON,
    PRODUKSJON_DLF,
    SLADDET,
    ORIGINAL
}