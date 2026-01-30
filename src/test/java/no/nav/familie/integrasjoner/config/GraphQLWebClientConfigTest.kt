package no.nav.familie.integrasjoner.config

import com.nimbusds.oauth2.sdk.GrantType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.log.NavHttpHeaders
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI

class GraphQLWebClientConfigTest {
    @Nested
    inner class ExchangeBearerTokenFilterTest {
        private val mockedClientConfigurationProperties: ClientConfigurationProperties = mockk()
        private val mockedOAuth2AccessTokenService: OAuth2AccessTokenService = mockk()
        private val exchangeTokenXBearerTokenFilter: GraphQLWebClientConfig.ExchangeTokenXBearerTokenFilter =
            GraphQLWebClientConfig.ExchangeTokenXBearerTokenFilter(
                clientConfigurationProperties = mockedClientConfigurationProperties,
                oAuth2AccessTokenService = mockedOAuth2AccessTokenService,
            )

        @Test
        fun `skal utstede nytt obo-token basert på url og matchende registrerte client i ClientProperties og sette det nye tokenet i Authorization-header`() {
            // Arrange
            val baseUrl = "https://test.no"
            val path = "/api/test"

            val mockedClientProperties: ClientProperties = mockk()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            val accessTokenResponse = OAuth2AccessTokenResponse("gyldig-token", 3600, null)
            val request = ClientRequest.create(HttpMethod.GET, URI.create(baseUrl + path)).build()
            val response = ClientResponse.create(HttpStatus.OK).build()

            val filteredRequestSlot = slot<ClientRequest>()

            every { mockedClientProperties.resourceUrl } returns URI.create(baseUrl)
            every { mockedClientProperties.grantType } returns GrantType.TOKEN_EXCHANGE
            every { mockedClientConfigurationProperties.registration.values } returns listOf(mockedClientProperties)
            every { mockedOAuth2AccessTokenService.getAccessToken(mockedClientProperties) } returns accessTokenResponse
            every { mockedExchangeFunction.exchange(capture(filteredRequestSlot)) } returns Mono.just(response)

            // Act
            exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()

            // Assert
            val capturedFilteredRequest = filteredRequestSlot.captured
            assertThat(capturedFilteredRequest.headers().toSingleValueMap())
                .containsKey("Authorization")
                .containsEntry("Authorization", "Bearer gyldig-token")
        }

        @Test
        fun `skal kaste exception dersom ingen client er registrert med matchende resourceUrl og grantType i ClientProperties`() {
            // Arrange
            val baseUrl = "https://test.no"
            val path = "/api/test"
            val request = ClientRequest.create(HttpMethod.GET, URI.create(baseUrl + path)).build()
            val mockedClientProperties: ClientProperties = mockk()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            every { mockedClientProperties.resourceUrl } returns URI.create(baseUrl)
            every { mockedClientProperties.grantType } returns GrantType.JWT_BEARER
            every { mockedClientConfigurationProperties.registration.values } returns listOf(mockedClientProperties)

            val exception =
                assertThrows<IllegalStateException> {
                    exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()
                }

            assertThat(exception.message).isEqualTo("Finner ikke ClientProperties for url:${baseUrl + path} og grantType: ${GrantType.TOKEN_EXCHANGE}")
        }

        @Test
        fun `skal kaste exception dersom hentet access token er null`() {
            // Arrange
            val baseUrl = "https://test.no"
            val path = "/api/test"

            val mockedClientProperties: ClientProperties = mockk()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            val accessTokenResponse = OAuth2AccessTokenResponse(null, 3600, null)
            val request = ClientRequest.create(HttpMethod.GET, URI.create(baseUrl + path)).build()

            every { mockedClientProperties.resourceUrl } returns URI.create(baseUrl)
            every { mockedClientProperties.grantType } returns GrantType.TOKEN_EXCHANGE
            every { mockedClientConfigurationProperties.registration.values } returns listOf(mockedClientProperties)
            every { mockedOAuth2AccessTokenService.getAccessToken(mockedClientProperties) } returns accessTokenResponse

            // Act & Assert
            val exception =
                assertThrows<JwtTokenValidatorException> {
                    exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()
                }
            assertThat(exception.message).isEqualTo("Kunne ikke hente accesstoken")
        }
    }

    @Nested
    inner class ConsumerIdFilter {
        @Test
        fun `skal legge til servicebruker som NAV_CONSUMER_ID i request headers når service bruker er satt`() {
            // Arrange
            val serviceUser = "test-service-user"
            val appName = "test-app"
            val consumerIdFilter = GraphQLWebClientConfig.ConsumerIdFilter(serviceUser, appName)
            val mockedExchangeFunction: ExchangeFunction = mockk()
            val request = ClientRequest.create(HttpMethod.GET, URI.create("https://test.no/api")).build()
            val response = ClientResponse.create(HttpStatus.OK).build()
            val filteredRequestSlot = slot<ClientRequest>()

            every { mockedExchangeFunction.exchange(capture(filteredRequestSlot)) } returns Mono.just(response)

            // Act
            consumerIdFilter.filter(request, mockedExchangeFunction).block()

            // Assert
            val capturedFilteredRequest = filteredRequestSlot.captured
            assertThat(capturedFilteredRequest.headers().toSingleValueMap())
                .containsKey(NavHttpHeaders.NAV_CONSUMER_ID.asString())
                .containsEntry(NavHttpHeaders.NAV_CONSUMER_ID.asString(), serviceUser)
        }

        @Test
        fun `skal bruke appnavn som NAV_CONSUMER_ID i request headers når service bruker ikke er satt`() {
            // Arrange
            val serviceUser = ""
            val appName = "test-app"
            val consumerIdFilter = GraphQLWebClientConfig.ConsumerIdFilter(serviceUser, appName)
            val mockedExchangeFunction: ExchangeFunction = mockk()
            val request = ClientRequest.create(HttpMethod.GET, URI.create("https://test.no/api")).build()
            val response = ClientResponse.create(HttpStatus.OK).build()
            val filteredRequestSlot = slot<ClientRequest>()

            every { mockedExchangeFunction.exchange(capture(filteredRequestSlot)) } returns Mono.just(response)

            // Act
            consumerIdFilter.filter(request, mockedExchangeFunction).block()

            // Assert
            val capturedFilteredRequest = filteredRequestSlot.captured
            assertThat(capturedFilteredRequest.headers().toSingleValueMap())
                .containsKey(NavHttpHeaders.NAV_CONSUMER_ID.asString())
                .containsEntry(NavHttpHeaders.NAV_CONSUMER_ID.asString(), appName)
        }
    }

    @Nested
    inner class CallIdFilter
}
