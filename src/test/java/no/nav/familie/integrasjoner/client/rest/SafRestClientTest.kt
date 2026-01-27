package no.nav.familie.integrasjoner.client.rest

import com.expediagroup.graphql.client.types.AutomaticPersistedQueriesSettings
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import java.net.URI

class SafRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val safRestClient: SafRestClient = SafRestClient(safBaseUrl = URI.create("pdl"), restTemplate = restOperations)

    @BeforeEach
    fun setUp() {
        mockkStatic(MDC::class)
        every { MDC.get(MDC_CALL_ID) } returns "123-321-412"
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

        every {
            restOperations.exchange<SafJournalpostResponse<SafJournalpostBrukerData>>(
                url = URI("pdl/graphql"),
                method = eq(HttpMethod.POST),
                requestEntity = any<HttpEntity<SafJournalpostRequest>>(),
            )
        } answers {
            ResponseEntity(response, HttpStatus.OK)
        }

        // Act
        val journalpostForbiddenException =
            assertThrows<JournalpostForbiddenException> {
                safRestClient.finnJournalposter(safJournalpostRequest)
            }

        // Assert
        assertThat(journalpostForbiddenException.message).isEqualTo("Feilmelding")
    }
}
