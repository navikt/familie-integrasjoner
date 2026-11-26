package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.client.HttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClient
import java.net.URI
import java.time.Duration

class SafHentDokumentRestClientTest {
    private val hybridRestClient: RestClient = mockk(relaxed = true)
    private val builder: RestClient.Builder = mockk(relaxed = true)
    private val requestFactories = mutableListOf<ClientHttpRequestFactory>()
    private val factory: EntraIDRestClientFactory =
        mockk {
            every { lagHybridRestKlient(any(), any()) } returns hybridRestClient
        }

    init {
        every { hybridRestClient.mutate() } returns builder
        every { builder.requestFactory(capture(requestFactories)) } returns builder
        every { builder.build() } returns mockk(relaxed = true)
    }

    private val safHentDokumentRestClient =
        SafHentDokumentRestClient(
            safBaseUrl = URI("http://localhost:8080/saf"),
            scope = "dummy-scope",
            entraIDRestClientFactory = factory,
        )

    @AfterEach
    fun tearDown() {
        safHentDokumentRestClient.destroy()
    }

    @Test
    fun `skal konfigurere klienten for dokumenthenting med 60 sekunders timeout`() {
        // Arrange + Act: skjer i konstruktøren
        val requestFactory = requestFactories.single()

        // Assert
        assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout")).isEqualTo(Duration.ofSeconds(60).toMillis())
        assertThat((ReflectionTestUtils.getField(requestFactory, "httpClient") as HttpClient).idleTimeout)
            .isEqualTo(Duration.ofSeconds(60).toMillis())
    }
}
