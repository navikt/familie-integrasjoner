package no.nav.familie.integrasjoner.medlemskap

import java.time.LocalDate

data class PeriodeSoekRequest(
    val personident: String,
    val type: String? = null,
    val statuser: List<String>? = null,
    val ekskluderKilder: List<String>? = null,
    val fraOgMed: LocalDate? = null,
    val tilOgMed: LocalDate? = null,
    val inkluderSporingsinfo: Boolean? = null,
)
