package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.net.URI
import no.nav.familie.integrasjoner.azure.domene.AzureAdBrukere
import no.nav.familie.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

class DokarkivRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val dokarkivRestClient: DokarkivRestClient = DokarkivRestClient(
        dokarkivUrl = URI("http://localhost:8080/dokarkiv"),
        restOperations = restOperations,
    )

    @BeforeEach
    fun setUp() {
        mockkStatic(MDC::class)
        every { MDC.get(MDC_CALL_ID) } returns "123-321-412"
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MDC::class)
    }

    @Nested
    inner class FerdigstillJournalpost {
        @Test
        fun `skal kaste OppslagException ved feil mot dokarkiv`() {
            // Arrange
            every { restOperations.exchange<String>(any<URI>(), eq(HttpMethod.PATCH), any<HttpEntity<FerdigstillJournalPost>>()) } throws RestClientResponseException(
                "Noe gikk galt",
                HttpStatus.NOT_FOUND,
                "bad request",
                null,
                null,
                null
            )

            // Act & Assert
            val oppslagException = assertThrows<OppslagException> { dokarkivRestClient.ferdigstillJournalpost("1234", "oslo", "4321") }

            assertThat(oppslagException.message).isEqualTo("Feil ved ferdigstilling av journalpost")
            assertThat(oppslagException.kilde).isEqualTo("dokarkiv.ferdigstill.feil")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}