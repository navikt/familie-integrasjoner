package no.nav.familie.integrasjoner.modiacontextholder

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderNyAktivBrukerDto
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["MODIA_CONTEXT_HOLDER_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class ModiaContextHolderControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.contentType = MediaType.APPLICATION_JSON
    }

    @Test
    fun `skal korrekt behandle returobjekt fra GET`() {
        stubFor(
            get("/api/context")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(modiaResponse()),
                ),
        )

        val response =
            restTemplate.exchange<Ressurs<ModiaContextHolderResponse>>(
                localhost("/api/modia-context-holder"),
                HttpMethod.GET,
                HttpEntity(dto(), headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.aktivBruker).isEqualTo("13025514402")
        assertThat(response.body?.data?.aktivEnhet).isEqualTo("0000")
    }

    @Test
    fun `skal korrekt behandle returobjekt fra POST`() {
        stubFor(
            post("/api/context")
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(modiaResponse()),
                ),
        )

        val response =
            restTemplate.exchange<Ressurs<ModiaContextHolderResponse>>(
                localhost("/api/modia-context-holder/sett-aktiv-bruker"),
                HttpMethod.POST,
                HttpEntity(dto(), headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.SUKSESS)
        assertThat(response.body?.data?.aktivBruker).isEqualTo("13025514402")
        assertThat(response.body?.data?.aktivEnhet).isEqualTo("0000")
    }

    @Test
    fun `skal håndtere feil korrekt for GET`() {
        stubFor(get("/api/context").willReturn(status(500)))

        val response =
            restTemplate.exchange<Ressurs<ModiaContextHolderResponse>>(
                url = localhost("/api/modia-context-holder"),
                method = HttpMethod.GET,
                requestEntity = HttpEntity<String>(headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    @Test
    fun `skal håndtere feil korrekt for POST`() {
        stubFor(post("/api/context").willReturn(status(500)))

        val response =
            restTemplate.exchange<Ressurs<ModiaContextHolderResponse>>(
                url = localhost("/api/modia-context-holder/sett-aktiv-bruker"),
                method = HttpMethod.POST,
                requestEntity = HttpEntity(dto(), headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.status).isEqualTo(Ressurs.Status.FEILET)
    }

    private fun modiaResponse(): String =
        objectMapper.writeValueAsString(
            ModiaContextHolderResponse(
                aktivBruker = "13025514402",
                aktivEnhet = "0000",
            ),
        )

    private fun dto() = ModiaContextHolderNyAktivBrukerDto(personIdent = "13025514402")
}
