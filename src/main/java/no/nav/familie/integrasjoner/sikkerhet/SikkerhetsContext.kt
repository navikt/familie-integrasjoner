package no.nav.familie.integrasjoner.sikkerhet

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SikkerhetsContext {
    private const val SYSTEM_NAVN = "System"
    const val SYSTEM_FORKORTELSE = "VL"

    fun hentSaksbehandlerEllerSystembruker(): String = hentClaim<String>("NAVident") ?: SYSTEM_FORKORTELSE

    fun hentSaksbehandlerNavn(strict: Boolean = false): String =
        hentClaim<String>("name")
            ?: if (strict) error("Finner ikke navn i azuread token") else SYSTEM_NAVN

    fun <T> hentClaim(claim: String): T? = hentJwt().getClaim(claim)

    fun hentJwt(): Jwt =
        (SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken)?.token
            ?: throw AuthenticationCredentialsNotFoundException("Klarte ikke hente token fra context")
}
