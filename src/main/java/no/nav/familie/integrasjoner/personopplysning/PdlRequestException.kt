package no.nav.familie.integrasjoner.personopplysning

open class PdlRequestException(melding: String? = null) : Exception(melding)

class PdlNotFoundException: PdlRequestException()