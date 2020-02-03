package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.dokdist.api.DistribuerJournalpostRequest
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockserver.junit.MockServerRule
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.boot.test.web.client.exchange
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts"])
class DokdistControllerTest : OppslagSpringRunnerTest() {

    @get:Rule
    val mockServerRule = MockServerRule(this, MOCK_SERVER_PORT)

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }


    @Test
    fun `dokdist returnerer OK`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(200)
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody("{\"bestillingsId\": \"1234567\"}"))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, "IT", "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK uten bestillingsId`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(200).withBody(""))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, "IT", "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("BestillingsId var null")
    }

    @Test
    fun `dokdist returnerer 400`() {
        mockServerRule.client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(400).withBody(badRequestResponse()))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, "IT", "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding)
                .contains("validering av distribusjonsforespørsel for journalpostId=453492547 feilet, feilmelding=")
    }

    @Throws(IOException::class) private fun badRequestResponse(): String {
        return Files.readString(ClassPathResource("dokdist/badrequest.json").file.toPath(),
                                StandardCharsets.UTF_8)
    }

    companion object {
        private const val MOCK_SERVER_PORT = 18321
        private const val DOKDIST_URL = "/api/dist/v1"
        private const val JOURNALPOST_ID = "453492547"
    }
}
