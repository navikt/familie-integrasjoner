package no.nav.familie.integrasjoner.medlemskap

import java.time.LocalDate

class MedlemskapsunntakResponse(
    val dekning: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val grunnlag: String,
    val ident: String,
    val medlem: Boolean,
    val status: String,
    val statusaarsak: String? = null
)
