package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.Unprotected
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.springframework.boot.test.web.client.exchange
import org.springframework.cache.Cache.ValueRetrievalException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ActiveProfiles("integrasjonstest", "mock-personopplysninger", "mock-oauth")
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
class ApiExceptionHandlerTest : OppslagSpringRunnerTest() {

    @Test
    internal fun `Skal håndtere cache exception`() {
        val response = gjørKallTilControllerSomKasterValueRetrievalException()
        assertThat(response.statusCode).isEqualTo(INTERNAL_SERVER_ERROR)
        assertThat(response.body?.stacktrace).contains("PdlNotFoundException")
    }

    private fun gjørKallTilControllerSomKasterValueRetrievalException() = restTemplate.exchange<Ressurs<String>>(
        localhost("/api/testfeil/"),
        HttpMethod.GET,
        HttpEntity<Ressurs<String>>(headers),
    )
}

@RestController
@RequestMapping("/api/testfeil/")
@Unprotected
class TestController {

    @GetMapping()
    fun kastValueRetrievalException(): Ressurs<String> {
        throw ValueRetrievalException("HemmeligIdent", {}, PdlNotFoundException())
    }
}
