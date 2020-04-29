package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlHentIdenterResponse(val data: PdlIdenter,
                                  val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }
}

data class PdlIdenter(val identer: List<IdentInformasjon>)

data class IdentInformasjon(val ident: String)
