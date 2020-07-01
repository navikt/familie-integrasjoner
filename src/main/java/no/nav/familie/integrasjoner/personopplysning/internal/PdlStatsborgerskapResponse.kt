package no.nav.familie.integrasjoner.personopplysning.internal

import java.time.LocalDate

data class PdlStatsborgerskapResponse(val data: Data,
                            val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }

    class Data(val person: Person?)
    class Person(val statsborgerskap: List<Statsborgerskap>)
}

data class Statsborgerskap(val land: String,
                           val gyldigFraOgMed: LocalDate?,
                           val gyldigTilOgMed: LocalDate?)





