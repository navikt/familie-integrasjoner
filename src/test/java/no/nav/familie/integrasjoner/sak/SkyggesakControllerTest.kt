package no.nav.familie.integrasjoner.sak

import com.github.tomakehurst.wiremock.client.WireMock.*
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.NavHttpHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.boot.test.web.client.postForObject
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-sts")
@TestPropertySource(properties = ["SKYGGE_SAK_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class SkyggesakControllerTest : OppslagSpringRunnerTest() {

    @Before
    fun setup() {
        headers.setBearerAuth(lokalTestToken)
        headers[NavHttpHeaders.NAV_CALL_ID.asString()] = "callIdTest"
    }

    @Test
    fun `skal opprette skyggesak i sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(okJson(objectMapper.writeValueAsString(request))))

        val response =
                restTemplate.postForObject<Ressurs<List<Nothing>>>(localhost("/api/skyggesak/v1"),
                                                                   HttpEntity(request,
                                                                              headers))

        verify(postRequestedFor(urlEqualTo("/api/v1/saker"))
                       .withRequestBody(equalToJson(objectMapper.writeValueAsString(request)))
                       .withHeader("X-Correlation-ID", equalTo("callIdTest")))

        assertThat(response?.status).isEqualTo(SUKSESS)
    }


    @Test
    fun `skal ikke feile hvis skyggesak alt er opprettet i sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(status(409)))

        val response =
                restTemplate.postForObject<Ressurs<List<Nothing>>>(localhost("/api/skyggesak/v1"),
                                                                   HttpEntity(request, headers))

        assertThat(response?.status).isEqualTo(SUKSESS)
    }

    @Test
    fun `skal kaste feil til klient når noe går galt mot Sak`() {
        val request = Skyggesak("BAR", "BA", "123", "321")
        stubFor(post(anyUrl()).willReturn(status(500).withBody("{\"feilmelding\" : \"Noe gikk galt\"}")))

        val response =
                restTemplate.postForObject<Ressurs<List<Nothing>>>(localhost("/api/skyggesak/v1"),
                                                                   HttpEntity(request, headers))

        assertThat(response?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response?.melding).contains("Noe gikk galt")
    }
}