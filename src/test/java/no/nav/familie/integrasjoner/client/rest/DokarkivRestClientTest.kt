package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.client.HttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import java.net.URI
import java.time.Duration

class DokarkivRestClientTest {
    private val restClient: RestClient = mockk(relaxed = true)
    private val journalpostRestClient: RestClient = mockk(relaxed = true)
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
        every { builder.build() } returnsMany listOf(restClient, journalpostRestClient)
    }

    private val dokarkivRestClient: DokarkivRestClient =
        DokarkivRestClient(
            dokarkivUrl = URI("http://localhost:8080/dokarkiv"),
            scope = "dummy-scope",
            entraIDRestClientFactory = factory,
        )

    @BeforeEach
    fun setUp() {
        mockkStatic(MDC::class)
        every { MDC.get(MDC_CALL_ID) } returns "123-321-412"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MDC::class)
        dokarkivRestClient.destroy()
    }

    @Test
    fun `skal konfigurere standardklienten med 60 sekunders timeout og journalpostklienten med 3 minutter`() {
        val (standardFactory, journalpostFactory) = requestFactories

        assertThat(readTimeout(standardFactory)).isEqualTo(Duration.ofSeconds(60).toMillis())
        assertThat(idleTimeout(standardFactory)).isEqualTo(Duration.ofSeconds(60).toMillis())
        assertThat(readTimeout(journalpostFactory)).isEqualTo(Duration.ofMinutes(3).toMillis())
        assertThat(idleTimeout(journalpostFactory)).isEqualTo(Duration.ofMinutes(3).toMillis())
    }

    @Test
    fun `skal bruke klient med lang timeout for oppretting av journalpost`() {
        every { journalpostRestClient.post() } throws ValgtKlient()

        assertThrows<ValgtKlient> {
            dokarkivRestClient.lagJournalpost(mockk(relaxed = true), ferdigstill = false)
        }

        verify(exactly = 1) { journalpostRestClient.post() }
        verify(exactly = 0) { restClient.post() }
    }

    @Test
    fun `skal bruke standardklienten for øvrige operasjoner`() {
        every { restClient.put() } throws ValgtKlient()
        every { restClient.patch() } throws ValgtKlient()

        assertThrows<ValgtKlient> { dokarkivRestClient.oppdaterJournalpost(mockk(relaxed = true), "1234") }
        assertThrows<ValgtKlient> { dokarkivRestClient.ferdigstillJournalpost("1234", "oslo", "4321") }
        assertThrows<ValgtKlient> { dokarkivRestClient.avsluttSak(mockk(relaxed = true)) }
        assertThrows<ValgtKlient> { dokarkivRestClient.gjenåpneSak(mockk(relaxed = true)) }

        verify(exactly = 1) { restClient.put() }
        verify(exactly = 3) { restClient.patch() }
        verify(exactly = 0) { journalpostRestClient.put() }
        verify(exactly = 0) { journalpostRestClient.patch() }
    }

    private fun readTimeout(factory: ClientHttpRequestFactory) = ReflectionTestUtils.getField(factory, "readTimeout")

    private fun idleTimeout(factory: ClientHttpRequestFactory) = (ReflectionTestUtils.getField(factory, "httpClient") as HttpClient).idleTimeout

    // Stopper kallet ved klientvalget uten at produksjonskodens feilhåndtering fanger det.
    private class ValgtKlient : Error()

    @Nested
    inner class FerdigstillJournalpost {
        @Test
        fun `skal kaste OppslagException ved feil mot dokarkiv`() {
            every {
                restClient
                    .patch()
                    .uri(any<URI>())
                    .headers(any())
                    .body(any<Any>())
                    .retrieve()
                    .hint(any(), any())
                    .body(any<ParameterizedTypeReference<String>>())
            } throws
                RestClientResponseException(
                    "Noe gikk galt",
                    HttpStatus.NOT_FOUND,
                    "bad request",
                    null,
                    null,
                    null,
                )

            val oppslagException =
                assertThrows<OppslagException> {
                    dokarkivRestClient.ferdigstillJournalpost("1234", "oslo", "4321")
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved ferdigstilling av journalpost")
            assertThat(oppslagException.kilde).isEqualTo("dokarkiv.ferdigstill.feil")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
