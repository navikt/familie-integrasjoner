package no.nav.familie.integrasjoner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.stereotype.Component

@Component("tokenXAuthenticationManager")
class TokenXAuthenticationManager(
    @Value("\${TOKEN_X_JWKS_URI}") jwksUri: String,
    @Value("\${TOKEN_X_ISSUER}") issuer: String,
    @Value("\${TOKEN_X_CLIENT_ID}") audience: String,
) : AuthenticationManager {
    private val providerManager: ProviderManager =
        run {
            val decoder =
                NimbusJwtDecoder.withJwkSetUri(jwksUri).build().also {
                    it.setJwtValidator(
                        JwtValidators.createDefaultWithValidators(
                            JwtIssuerValidator(issuer),
                            JwtAudienceValidator(audience),
                            JwtClaimValidator<String>("acr") { acr -> acr == "Level4" || acr == "idporten-loa-high" },
                        ),
                    )
                }
            ProviderManager(JwtAuthenticationProvider(decoder))
        }

    override fun authenticate(authentication: Authentication): Authentication = providerManager.authenticate(authentication)
}
