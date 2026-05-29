package no.nav.familie.integrasjoner.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtAudienceValidator
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtValidators.createDefaultWithValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.stereotype.Component

@Primary
@Component("azureAdAuthenticationManager")
class AzureAdAuthenticationManager(
    @Value("\${AZURE_OPENID_CONFIG_JWKS_URI}") jwksUri: String,
    @Value("\${AZURE_OPENID_CONFIG_ISSUER}") issuer: String,
    @Value("\${AZURE_APP_CLIENT_ID}") audience: String,
) : AuthenticationManager {
    private val providerManager: ProviderManager =
        run {
            val decoder =
                NimbusJwtDecoder.withJwkSetUri(jwksUri).build().also {
                    it.setJwtValidator(
                        createDefaultWithValidators(
                            JwtIssuerValidator(issuer),
                            JwtAudienceValidator(audience),
                        ),
                    )
                }
            ProviderManager(JwtAuthenticationProvider(decoder))
        }

    override fun authenticate(authentication: Authentication): Authentication = providerManager.authenticate(authentication)
}
