package no.nav.familie.integrasjoner.dokdistkanal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.status
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.dokdistkanal.Distribusjonskanal
import no.nav.familie.kontrakter.felles.dokdistkanal.DokdistkanalRequest
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.NavHttpHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.postForObject
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["DOKDISTKANAL_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class DokdistkanalControllerTest : OppslagSpringRunnerTest() {

    @BeforeEach
    fun setup() {
        headers.setBearerAuth(lokalTestToken)
        headers[NavHttpHeaders.NAV_CALL_ID.asString()] = "callIdTest"
    }

    @Test
    fun `dokdistkanal returnerer OK med distribusjonskanal`() {
        val request = DokdistkanalRequest(
            bruker = PersonIdent(BRUKER_ID),
            mottaker = PersonIdent(BRUKER_ID),
        )
        val gyldigDokdistkanalRespons = BestemDistribusjonskanalResponse(
            distribusjonskanal = "PRINT",
            regel = "PERSON_ER_IKKE_I_PDL",
            regelBegrunnelse = "Finner ikke personen i PDL",
        )

        stubFor(post(anyUrl()).willReturn(okJson(objectMapper.writeValueAsString(gyldigDokdistkanalRespons))))

        val response =
            restTemplate.postForObject<Ressurs<String>>(
                localhost(DOKDISTKANAL_URL),
                HttpEntity(request, headers),
            )

        verify(
            postRequestedFor(urlEqualTo("/rest/bestemDistribusjonskanal"))
                .withHeader("X-Correlation-ID", equalTo("callIdTest")),
        )

        assertThat(response?.status).isEqualTo(SUKSESS)
        assertThat(response?.data).isEqualTo(gyldigDokdistkanalRespons.distribusjonskanal)
        assertThat(response?.melding).isEqualTo(gyldigDokdistkanalRespons.regelBegrunnelse)
    }

    @Test
    fun `skal kaste feil til klient når noe går galt mot tjenesten`() {
        val request = DokdistkanalRequest(
            bruker = PersonIdent(BRUKER_ID),
            mottaker = PersonIdent(BRUKER_ID),
        )
        stubFor(post(anyUrl()).willReturn(status(500).withBody("{\"feilmelding\" : \"Noe gikk galt\"}")))

        val response =
            restTemplate.postForObject<Ressurs<String>>(
                localhost(DOKDISTKANAL_URL),
                HttpEntity(request, headers),
            )

        assertThat(response?.status).isEqualTo(Ressurs.Status.FEILET)
        assertThat(response?.melding).contains("Noe gikk galt")
    }

    @Test
    fun `skal returnere UKJENT og logge feil dersom familie-kontrakter mangler verdien returnert av dokdistkanal`() {
        (LoggerFactory.getLogger(DokdistkanalController::class.java) as Logger).addAppender(listAppender)

        stubFor(
            post(anyUrl()).willReturn(
                okJson(
                    objectMapper.writeValueAsString(
                        BestemDistribusjonskanalResponse(
                            distribusjonskanal = "NY_KANAL",
                            regel = "REGEL",
                            regelBegrunnelse = "regelbegrunnelse",
                        ),
                    ),
                ),
            ),
        )

        val response = restTemplate.postForObject<Ressurs<String>>(
            localhost(DOKDISTKANAL_URL),
            HttpEntity(
                /* body = */
                DokdistkanalRequest(
                    bruker = PersonIdent(BRUKER_ID),
                    mottaker = PersonIdent(BRUKER_ID),
                ),
                /* headers = */
                headers,
            ),
        )

        assertThat(response?.status).isEqualTo(SUKSESS)
        assertThat(response?.data).isEqualTo(Distribusjonskanal.UKJENT.name)
        assertThat(loggingEvents).extracting<Level> { it.level }
            .containsExactly(Level.ERROR)
        assertThat(loggingEvents).extracting<String> { it.formattedMessage }
            .containsExactly("Distribusjonskanal-kontrakten er utdatert og må oppdateres med ny verdi for NY_KANAL")
    }

    companion object {
        private const val DOKDISTKANAL_URL = "/api/dokdistkanal/BAR"
        private const val BRUKER_ID = "12345678910"
    }
}
