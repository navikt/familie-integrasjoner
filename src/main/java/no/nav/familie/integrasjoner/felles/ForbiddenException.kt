package no.nav.familie.integrasjoner.felles

class ForbiddenException(override val message: String = "Du har ikke tilgang til handling") : Exception(message)