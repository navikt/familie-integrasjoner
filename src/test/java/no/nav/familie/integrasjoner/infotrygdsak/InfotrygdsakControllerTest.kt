package no.nav.familie.integrasjoner.infotrygdsak

import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.infotrygdsak.FinnInfotrygdSakerRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.InfotrygdSak
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@ActiveProfiles("integrasjonstest", "mock-sts")
@TestPropertySource(properties = ["GOSYS_INFOTRYGDSAK_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
internal class InfotrygdsakControllerTest : OppslagSpringRunnerTest() {

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
        headers.set("Content-Type", "application/json")
    }

    @Test
    fun `skal korrekt behandle request`() {
        val xmlResponse = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                   |   <soapenv:Body>
                   |      <sak:bestillInfotrygdSakResponse xmlns:sak="http://nav-cons-sak-gosys-3.0.0/no/nav/inf/InfotrygdSak">
                   |         <bestillSakResponse>
                   |            <saksId>B01</saksId>
                   |            <status>OK</status>
                   |            <bekreftelsesbrevSendt>false</bekreftelsesbrevSendt>
                   |         </bestillSakResponse>
                   |      </sak:bestillInfotrygdSakResponse>
                   |   </soapenv:Body>
                   |</soapenv:Envelope>""".trimMargin()
        WireMock.stubFor(WireMock.post(WireMock.anyUrl()).willReturn(WireMock.okXml(xmlResponse)))
        val opprettInfotrygdSakRequest = OpprettInfotrygdSakRequest(fnr = "10108000398",
                                                                    fagomrade = "ENF",
                                                                    stonadsklassifisering2 = "OG",
                                                                    type = "K",
                                                                    opprettetAv = "B900001",
                                                                    opprettetAvOrganisasjonsEnhetsId = "4408",
                                                                    mottakerOrganisasjonsEnhetsId = "4408",
                                                                    mottattdato = LocalDate.of(2013, 9, 13),
                                                                    sendBekreftelsesbrev = false,
                                                                    oppgaveId = "137649517",
                                                                    oppgaveOrganisasjonsenhetId = "4408")

        val response: ResponseEntity<Ressurs<OpprettInfotrygdSakResponse>> =
                restTemplate.exchange(localhost("/api/infotrygdsak/opprett"),
                                      HttpMethod.POST,
                                      HttpEntity<Any>(objectMapper.writeValueAsString(opprettInfotrygdSakRequest), headers))

        Assertions.assertThat(response.body!!.data).isNotNull()

    }


    @Test
    fun `skal hente liste med infotrygdsaker`() {
        val xmlResponse = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                   |   <soapenv:Body>
                   |      <sak:hentSakListeResponse xmlns:sak="http://nav-cons-sak-gosys-3.0.0/no/nav/inf/InfotrygdSak">
                   |         <hentSakListeResponse>
                   |            <sakListe>
                   |                <gjelderId>12345678901</gjelderId>
                   |                <saksnr>B01</saksnr>
                   |                <fagomradeKode>B01</fagomradeKode>
                   |                <registrertNavEnhetId>0385</registrertNavEnhetId>
                   |            </sakListe>
                   |        </hentSakListeResponse>
                   |      </sak:hentSakListeResponse>
                   |   </soapenv:Body>
                   |</soapenv:Envelope>""".trimMargin()
        WireMock.stubFor(WireMock.post(WireMock.anyUrl()).willReturn(WireMock.okXml(xmlResponse)))
        val finnInfotrygdsaker = FinnInfotrygdSakerRequest(fnr = "10108000398", fagomrade = "ENF")

        val response: ResponseEntity<Ressurs<List<InfotrygdSak>>> =
                restTemplate.exchange(localhost("/api/infotrygdsak/soek"),
                                      HttpMethod.POST,
                                      HttpEntity<Any>(objectMapper.writeValueAsString(finnInfotrygdsaker), headers))

        Assertions.assertThat(response.body!!.data).isNotNull()

    }
}