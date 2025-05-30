package no.nav.familie.integrasjoner.config

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GraphQLClientConfig(
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val oAuth2Config: ClientConfigurationProperties,
) {
    @Bean("SafSelvbetjening")
    fun graphqlSafSelvbetjeningClient(
        @Value("\${SAF_SELVBETJENING_URL}") safSelvbetjeningURI: String,
        bearerTokenClientInterceptor: BearerTokenClientInterceptor,
    ): GraphQLWebClient {
        val clientProperties: ClientProperties =
            oAuth2Config.registration["saf-selvbetjening-onbehalf-of"] ?: throw IllegalStateException("Finner ikke ClientProperties for 'saf-selvbetjening-onbehalf-of'")
        return GraphQLWebClient(
            url = "$safSelvbetjeningURI/graphql",
            builder =
                WebClient.builder().filter(
                    exchangeBearerTokenFilter(clientProperties),
                ),
        )
    }

    private fun exchangeBearerTokenFilter(clientProperties: ClientProperties) =
        ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            val accessToken: String =
                oAuth2AccessTokenService.getAccessToken(clientProperties).access_token
                    ?: throw IllegalStateException("Access token mangler")

            val filtered =
                ClientRequest
                    .from(request)
                    .headers {
                        it.setBearerAuth(accessToken)
                    }.build()

            next.exchange(filtered)
        }
}
