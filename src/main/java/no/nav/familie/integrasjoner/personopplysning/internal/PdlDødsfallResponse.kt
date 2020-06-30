package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlDødsfallResponse(val data: Data,
                               val errors: List<PdlError>?) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }

    fun errorMessages(): String {
        return errors?.joinToString { it -> it.message } ?: ""
    }

    class Data(val person: Person?)
    class Person(val doedsfall: List<Doedsfall>)
}

class Doedsfall(val doedsdato: String?)





