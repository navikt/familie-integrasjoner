package no.nav.familie.integrasjoner.journalpost.selvbetjening

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.config.ApiExceptionHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.HttpServerErrorException

class JournalpostSelvbetjeningControllerExceptionTest {
    private val safSelvbetjeningService = mockk<SafSelvbetjeningService>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(JournalpostSelvbetjeningController(safSelvbetjeningService))
                .setControllerAdvice(ApiExceptionHandler())
                .build()
    }

    @Test
    fun `feil fra SAF selvbetjening gir JSON-feilsvar selv om klienten ber om PDF`() {
        val tokenXFeil =
            HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                HttpHeaders(),
                """{"error":"server_error","error_description":"Identity provider request failed"}""".toByteArray(),
                null,
            )
        every { safSelvbetjeningService.hentDokument("123", "456") } throws
            SafSelvbetjeningException("Ukjent feil ved henting av dokument.", tokenXFeil)

        mockMvc
            .get("/api/journalpostselvbetjening/123/dokument/456") {
                accept = MediaType.APPLICATION_PDF
            }.andExpect {
                status { isInternalServerError() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value("FEILET") }
                jsonPath("$.melding") { value("Feil ved henting av dokument fra SAF selvbetjening") }
            }
    }
}
