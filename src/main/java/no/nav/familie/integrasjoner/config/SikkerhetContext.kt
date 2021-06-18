package no.nav.familie.integrasjoner.config

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder

object SikkerhetContext {

    const val SYSTEM_FORKORTELSE = "VL"

    fun hentSaksbehandler(): String {
        return Result.runCatching { SpringTokenValidationContextHolder().tokenValidationContext }
                .fold(
                        onSuccess = { it.getClaims("azuread")?.get("preferred_username")?.toString() ?: SYSTEM_FORKORTELSE },
                        onFailure = { SYSTEM_FORKORTELSE }
                )
    }

    fun erSystemKontekst() = hentSaksbehandler() == SYSTEM_FORKORTELSE
}