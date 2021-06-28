package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
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
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class DokdistControllerTest(val client: ClientAndServer) : OppslagSpringRunnerTest() {

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }


    @Test
    fun `dokdist returnerer OK`() {
        client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(200)
                                 .withHeader("Content-Type", "application/json;charset=UTF-8")
                                 .withBody("{\"bestillingsId\": \"1234567\"}"))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        Assertions.assertThat(response.body?.data).contains("1234567")
    }

    @Test
    fun `dokdist returnerer OK uten bestillingsId`() {
        client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(200).withBody(""))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding).contains("BestillingsId var null")
    }

    @Test
    fun `dokdist returnerer 400`() {
        client
                .`when`(HttpRequest.request()
                                .withMethod("POST")
                                .withPath("/rest/v1/distribuerjournalpost"))
                .respond(HttpResponse.response().withStatusCode(400)
                                 .withHeader("Content-Type", "application/json; charset=utf-8")
                                 .withBody(badRequestResponse()))

        val body = DistribuerJournalpostRequest(JOURNALPOST_ID, Fagsystem.BA, "ba-sak")
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(localhost(DOKDIST_URL),
                                                                              HttpMethod.POST,
                                                                              HttpEntity(body, headers))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        Assertions.assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        Assertions.assertThat(response.body?.melding)
                .contains("validering av distribusjonsforespørsel for journalpostId=453492547 feilet, feilmelding=")
    }

    @Throws(IOException::class) private fun badRequestResponse(): String {
        return Files.readString(ClassPathResource("dokdist/badrequest.json").file.toPath(), StandardCharsets.UTF_8)
    }

    companion object {
        private const val DOKDIST_URL = "/api/dist/v1"
        private const val JOURNALPOST_ID = "453492547"
    }
}
