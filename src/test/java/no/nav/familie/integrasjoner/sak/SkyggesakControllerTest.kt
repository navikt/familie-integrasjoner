package no.nav.familie.integrasjoner.sak

import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.NavHttpHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.postForObject
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@ActiveProfiles("integrasjonstest", "mock-sts")
@TestPropertySource(properties = ["SKYGGE_SAK_URL=http://localhost:28085"])
@EnableWireMock(
    ConfigureWireMock(name = "SkyggesakControllerTest", port = 28085),
)
class SkyggesakControllerTest : OppslagSpringRunnerTest() {
    @BeforeEach
    fun setup() {
        headers.setBearerAuth(lokalTestToken)
        headers[NavHttpHeaders.NAV_CALL_ID.asString()] = "callIdTest"
    }

    @Test
    fun `skal opprette skyggesak i sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(okJson(objectMapper.writeValueAsString(request))))

        val response =
            restTemplate.postForObject<Ressurs<List<Nothing>>>(
                localhost("/api/skyggesak/v1"),
                HttpEntity(
                    request,
                    headers,
                ),
            )

        verify(
            postRequestedFor(urlEqualTo("/api/v1/saker"))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
                .withHeader("X-Correlation-ID", equalTo("callIdTest")),
        )

        assertThat(response?.status.toString()).isEqualTo(SUKSESS.toString())
    }

    @Test
    fun `skal ikke feile hvis skyggesak alt er opprettet i sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(status(409)))

        val response =
            restTemplate.postForObject<Ressurs<List<Nothing>>>(
                localhost("/api/skyggesak/v1"),
                HttpEntity(request, headers),
            )

        assertThat(response?.status).isEqualTo(SUKSESS)
    }

    @Test
    fun `skal kaste feil til klient når noe går galt mot Sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(status(500).withBody("{\"feilmelding\" : \"Noe gikk galt\"}")))

        val response =
            restTemplate.postForObject<Ressurs<List<Nothing>>>(
                localhost("/api/skyggesak/v1"),
                HttpEntity(request, headers),
            )

        assertThat(response?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response?.melding).contains("Noe gikk galt")
    }
}
