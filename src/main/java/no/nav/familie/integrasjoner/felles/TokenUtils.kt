package no.nav.familie.integrasjoner.felles

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

fun erSystembruker(): Boolean {
    return try {
        val preferred_username =
                SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")["preferred_username"]
        return preferred_username == null
    } catch (e: Throwable) {
        // Ingen request context. Skjer ved kall som har opphav i kjørende applikasjon. Ping etc.
        true
    }
}
