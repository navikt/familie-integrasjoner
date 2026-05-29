package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.MockOAuth2ServerInitializer
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@ActiveProfiles("integrasjonstest", "mock-oauth")
class SecurityConfigTest : OppslagSpringRunnerTest() {
    @Test
    fun `api-ping er tilgjengelig uten token`() {
        val response =
            restTemplate.exchange<String>(
                localhost("/api/ping"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isNotIn(UNAUTHORIZED, FORBIDDEN)
    }

    @Test
    fun `internal-endepunkt er tilgjengelig uten token`() {
        val response =
            restTemplate.exchange<String>(
                localhost("/internal/health"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isNotIn(UNAUTHORIZED, FORBIDDEN)
    }

    @Test
    fun `beskyttet endepunkt returnerer 401 uten token`() {
        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/organisasjon/123456789"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `beskyttet endepunkt returnerer 401 med ugyldig token`() {
        headers.setBearerAuth("dette.er.ikke.et.gyldig.jwt")

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/organisasjon/123456789"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `beskyttet endepunkt passerer autentisering med gyldig Azure AD-token`() {
        headers.setBearerAuth(lokalTestToken)

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/organisasjon/123456789"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isNotIn(UNAUTHORIZED, FORBIDDEN)
    }

    @Test
    fun `journalpostselvbetjening returnerer 401 uten token`() {
        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/journalpostselvbetjening/dokumentoversikt/BAR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `journalpostselvbetjening returnerer 401 med Azure AD-token`() {
        // Azure AD-token er ikke gyldig for TokenX-kjeden
        headers.setBearerAuth(lokalTestToken)

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/journalpostselvbetjening/dokumentoversikt/BAR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
    }

    @Test
    fun `journalpostselvbetjening passerer autentisering med gyldig TokenX-token`() {
        headers.setBearerAuth(lagTokenXToken())

        val response: ResponseEntity<String> =
            restTemplate.exchange(
                localhost("/api/journalpostselvbetjening/dokumentoversikt/BAR"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isNotIn(UNAUTHORIZED, FORBIDDEN)
    }

    @Test
    fun `401-respons inneholder korrekt JSON-body`() {
        val response: ResponseEntity<Ressurs<Nothing>> =
            restTemplate.exchange(
                localhost("/api/organisasjon/123456789"),
                HttpMethod.GET,
                HttpEntity<Any>(headers),
            )

        assertThat(response.statusCode).isEqualTo(UNAUTHORIZED)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response.body?.melding).contains("401 Unauthorized")
        assertThat(response.body?.frontendFeilmelding).isEqualTo("Kall ikke autorisert")
    }

    private fun lagTokenXToken(): String {
        val clientId = UUID.randomUUID().toString()
        val brukerId = UUID.randomUUID().toString()
        val issuerId = "tokenx"
        return MockOAuth2ServerInitializer.server
            .issueToken(
                issuerId = issuerId,
                clientId = clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = issuerId,
                    subject = brukerId,
                    audience = listOf("aud-localhost"),
                    claims =
                        mapOf(
                            "acr" to "Level4",
                            "pid" to brukerId,
                        ),
                    expiry = 3600,
                ),
            ).serialize()
    }
}
