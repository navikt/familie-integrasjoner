package no.nav.familie.integrasjoner.config

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.familie.log.IdUtils
import no.nav.familie.log.NavHttpHeaders
import no.nav.familie.log.mdc.MDCConstants
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.MDC
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
        @Value("\${application.name}") appName: String,
        @Value("\${credential.username:}") serviceUser: String,
    ): GraphQLWebClient {
        val clientProperties: ClientProperties =
            oAuth2Config.registration["saf-selvbetjening-onbehalf-of"] ?: throw IllegalStateException("Finner ikke ClientProperties for 'saf-selvbetjening-onbehalf-of'")
        return GraphQLWebClient(
            url = "$safSelvbetjeningURI/graphql",
            builder =
                WebClient
                    .builder()
                    .filter(
                        exchangeBearerTokenFilter(clientProperties),
                    ).filter(
                        consumerIdFilter(serviceUser = serviceUser, appName = appName),
                    ).filter(
                        callIdFilter(),
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

    private fun consumerIdFilter(
        serviceUser: String,
        appName: String,
    ) = ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
        val filtered =
            ClientRequest
                .from(request)
                .headers {
                    it.set(NavHttpHeaders.NAV_CONSUMER_ID.asString(), serviceUser.ifBlank { appName })
                }.build()

        next.exchange(filtered)
    }

    private fun callIdFilter() =
        ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
            val filtered =
                ClientRequest
                    .from(request)
                    .headers {
                        it.set(
                            NavHttpHeaders.NAV_CALL_ID.asString(),
                            MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId(),
                        )
                    }.build()

            next.exchange(filtered)
        }
}
