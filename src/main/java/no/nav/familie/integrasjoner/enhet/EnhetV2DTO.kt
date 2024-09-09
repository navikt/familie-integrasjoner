package no.nav.familie.integrasjoner.enhet

data class EnhetV2DTO(
    val enhetId: String,
    val temaer: List<String>,
    val navn: String
)