package no.nav.familie.integrasjoner.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.log.NavHttpHeaders
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
        private val tokenXClient: TokenXClient = mockk()
        private val exchangeTokenXBearerTokenFilter =
            GraphQLWebClientConfig.ExchangeTokenXBearerTokenFilter(
                tokenXClient = tokenXClient,
                scope = "api://test-scope/.default",
            )

        @BeforeEach
        fun setUp() {
            mockkObject(EksternBrukerUtils)
        }

        @AfterEach
        fun tearDown() {
            unmockkObject(EksternBrukerUtils)
        }

        @Test
        fun `skal veksle brukertoken via TokenXClient og sette nytt token i Authorization-header`() {
            // Arrange
            val request = ClientRequest.create(HttpMethod.GET, URI.create("https://test.no/api/test")).build()
            val response = ClientResponse.create(HttpStatus.OK).build()
            val filteredRequestSlot = slot<ClientRequest>()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            every { EksternBrukerUtils.getBearerTokenForLoggedInUser() } returns "bruker-token"
            every { tokenXClient.hentToken("bruker-token", "api://test-scope/.default") } returns "vekslet-token"
            every { mockedExchangeFunction.exchange(capture(filteredRequestSlot)) } returns Mono.just(response)

            // Act
            exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()

            // Assert
            val capturedRequest = filteredRequestSlot.captured
            assertThat(capturedRequest.headers().toSingleValueMap())
                .containsEntry("Authorization", "Bearer vekslet-token")
        }

        @Test
        fun `skal propagere exception dersom henting av brukertoken feiler`() {
            // Arrange
            val request = ClientRequest.create(HttpMethod.GET, URI.create("https://test.no/api/test")).build()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            every { EksternBrukerUtils.getBearerTokenForLoggedInUser() } throws IllegalStateException("Ingen innlogget bruker")

            // Act & Assert
            assertThrows<IllegalStateException> {
                exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()
            }
        }

        @Test
        fun `skal propagere exception dersom TokenXClient feiler`() {
            // Arrange
            val request = ClientRequest.create(HttpMethod.GET, URI.create("https://test.no/api/test")).build()
            val mockedExchangeFunction: ExchangeFunction = mockk()

            every { EksternBrukerUtils.getBearerTokenForLoggedInUser() } returns "bruker-token"
            every { tokenXClient.hentToken(any(), any()) } throws RuntimeException("Token exchange feilet")

            // Act & Assert
            assertThrows<RuntimeException> {
                exchangeTokenXBearerTokenFilter.filter(request, mockedExchangeFunction).block()
            }
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
}
