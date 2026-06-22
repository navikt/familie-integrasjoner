package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import java.net.URI

class DokarkivRestClientTest {
    private val restClient: RestClient = mockk(relaxed = true)
    private val factory: EntraIDRestClientFactory =
        mockk {
            every { lagOboRestKlient(any(), any()) } returns restClient
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
    }

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
