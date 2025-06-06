package no.nav.familie.integrasjoner.config

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.nimbusds.oauth2.sdk.GrantType
import no.nav.familie.log.IdUtils
import no.nav.familie.log.NavHttpHeaders
import no.nav.familie.log.mdc.MDCConstants
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class GraphQLWebClientConfig {
    @Bean("SafSelvbetjening")
    fun graphqlSafSelvbetjeningClient(
        @Value("\${SAF_SELVBETJENING_URL}") safSelvbetjeningURI: String,
        @Qualifier("TokenXWebClient") tokenXWebClientBuilder: WebClient.Builder,
    ): GraphQLWebClient =
        GraphQLWebClient(
            url = "$safSelvbetjeningURI/graphql",
            builder = tokenXWebClientBuilder,
        )

    @Bean("TokenXWebClient")
    fun tokenXWebClientBuilder(
        exchangeTokenXBearerTokenFilter: ExchangeTokenXBearerTokenFilter,
        consumerIdFilter: ConsumerIdFilter,
        callIdFilter: CallIdFilter,
    ): WebClient.Builder =
        WebClient
            .builder()
            .filter(exchangeTokenXBearerTokenFilter)
            .filter(consumerIdFilter)
            .filter(callIdFilter)

    @Component
    class ExchangeTokenXBearerTokenFilter(
        private val clientConfigurationProperties: ClientConfigurationProperties,
        private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    ) : ExchangeFilterFunction {
        override fun filter(
            request: ClientRequest,
            next: ExchangeFunction,
        ): Mono<ClientResponse> {
            val clientProperties =
                clientConfigurationProperties.registration.values
                    .firstOrNull { request.url().toString().contains(it.resourceUrl.toString()) && it.grantType == GrantType.TOKEN_EXCHANGE }
                    ?: error("Finner ikke ClientProperties for url:${request.url()} og grantType: ${GrantType.TOKEN_EXCHANGE}")

            val accessToken: String =
                oAuth2AccessTokenService.getAccessToken(clientProperties).access_token
                    ?: throw JwtTokenValidatorException("Kunne ikke hente accesstoken")
            val filtered =
                ClientRequest
                    .from(request)
                    .headers {
                        it.setBearerAuth(accessToken)
                    }.build()

            return next.exchange(filtered)
        }
    }

    @Component
    class ConsumerIdFilter(
        @Value("\${credential.username:}")private val serviceUser: String,
        @Value("\${application.name}") private val appName: String,
    ) : ExchangeFilterFunction {
        override fun filter(
            request: ClientRequest,
            next: ExchangeFunction,
        ): Mono<ClientResponse> {
            val filtered =
                ClientRequest
                    .from(request)
                    .headers {
                        it.set(NavHttpHeaders.NAV_CONSUMER_ID.asString(), serviceUser.ifBlank { appName })
                    }.build()

            return next.exchange(filtered)
        }
    }

    @Component
    class CallIdFilter : ExchangeFilterFunction {
        override fun filter(
            request: ClientRequest,
            next: ExchangeFunction,
        ): Mono<ClientResponse> {
            val filtered =
                ClientRequest
                    .from(request)
                    .headers {
                        it.set(
                            NavHttpHeaders.NAV_CALL_ID.asString(),
                            MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId(),
                        )
                    }.build()

            return next.exchange(filtered)
        }
    }
}
