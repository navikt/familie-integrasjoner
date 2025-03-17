package no.nav.familie.integrasjoner.modiacontextholder.domene

data class ModiaContextHolderRequest(
    val verdi: String,
    val eventType: ModiaContextEventType,
)

enum class ModiaContextEventType {
    NY_AKTIV_BRUKER,
    NY_AKTIV_ENHET,
}
