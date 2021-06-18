package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.net.URI

@ActiveProfiles("integrasjonstest", "mock-saf", "mock-oauth")
class HentDokumentTest : OppslagSpringRunnerTest() {

    @Test
    fun `Skal returnere 403 ved henting av dokument med systembruker`() {
        val response = restTemplate.exchange<ByteArray>(URI.create(hentUrl("/api/journalpost/hentdokument/12345678/12345678")),
                                                        HttpMethod.GET,
                                                        HttpEntity(null,
                                                                   HttpHeaders().apply { setBearerAuth(token()) }))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Skal returnere 200 OK ved henting av dokument med saksbehandler`() {
        val response = restTemplate.exchange<ByteArray>(URI.create(hentUrl("/api/journalpost/hentdokument/12345678/12345678")),
                                                        HttpMethod.GET,
                                                        HttpEntity(null,
                                                                   HttpHeaders().apply {
                                                                       setBearerAuth(token(mapOf("groups" to listOf("SAKSBEHANDLER"),
                                                                                                 "name" to "Mock McMockface",
                                                                                                 "preferred_username" to "mock.mcmockface@nav.no")))
                                                                   }))

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}