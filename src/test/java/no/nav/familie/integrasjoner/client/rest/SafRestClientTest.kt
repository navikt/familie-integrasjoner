package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.internal.SafError
import no.nav.familie.integrasjoner.journalpost.internal.SafErrorCode
import no.nav.familie.integrasjoner.journalpost.internal.SafExtension
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostBrukerData
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestForBruker
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestClient
import java.net.URI

class SafRestClientTest {
    private val restClient: RestClient = mockk()
    private val requestBodyUriSpec: RestClient.RequestBodyUriSpec = mockk()
    private val requestBodySpec: RestClient.RequestBodySpec = mockk()
    private val responseSpec: RestClient.ResponseSpec = mockk()
    private val safRestClient: SafRestClient =
        SafRestClient(
            safBaseUrl = URI.create("http://saf"),
            restClient = restClient,
        )

    @BeforeEach
    fun setUp() {
        mockkStatic(MDC::class)
        every { MDC.get(MDC_CALL_ID) } returns "123-321-412"

        every { restClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<URI>()) } returns requestBodySpec
        every { requestBodySpec.header(any(), any()) } returns requestBodySpec
        every { requestBodySpec.body(any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
    }

    @Test
    fun finnJournalposter() {
        // Arrange
        val safJournalpostRequest =
            SafJournalpostRequest(
                SafRequestForBruker(
                    brukerId = Bruker("12345678901", BrukerIdType.FNR),
                    antall = 10,
                    tema = listOf(Tema.BAR),
                    journalposttype = listOf(Journalposttype.I),
                    journalstatus = emptyList(),
                ),
                graphqlQuery("/saf/journalposterForBruker.graphql"),
            )

        val response =
            SafJournalpostResponse<SafJournalpostBrukerData>(
                data = null,
                errors = listOf(SafError(message = "Feilmelding", extensions = SafExtension(code = SafErrorCode.forbidden, classification = "Mangler tilgang"))),
            )

        every { responseSpec.body(any<ParameterizedTypeReference<SafJournalpostResponse<SafJournalpostBrukerData>>>()) } returns response

        // Act
        val journalpostForbiddenException =
            assertThrows<JournalpostForbiddenException> {
                safRestClient.finnJournalposter(safJournalpostRequest)
            }

        // Assert
        assertThat(journalpostForbiddenException.message).isEqualTo("Feilmelding")
    }
}
