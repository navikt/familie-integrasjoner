package no.nav.familie.integrasjoner.kodeverk

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.HierarkiGeografiInnlandDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkSpråk
import no.nav.familie.kontrakter.felles.kodeverk.LandDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@ActiveProfiles("integrasjonstest", "mock-oauth")
@TestPropertySource(properties = ["KODEVERK_URL=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class KodeverkControllerTest : OppslagSpringRunnerTest() {
    @Test
    fun `skal hente landkoder for EØS`() {
        val beskrivelseFoo = BeskrivelseDto("Foo", "")
        val betydningFoo = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelseFoo))
        val beskrivelseBar = BeskrivelseDto("Bar", "")
        val betydningBar = BetydningDto(LocalDate.now(), LocalDate.now(), mapOf(KodeverkSpråk.BOKMÅL.kode to beskrivelseBar))
        val kodeverk = KodeverkDto(mapOf("FOO" to listOf(betydningFoo), "BAR" to listOf(betydningBar)))

        stubFor(
            get(GET_KODEVERK_EEAFREG_URL).willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(kodeverk)),
            ),
        )

        val response: ResponseEntity<Ressurs<KodeverkDto>> =
            restTemplate.exchange(
                localhost(KODEVERK_EEARG_URL),
                HttpMethod.GET,
                null,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data?.betydninger).hasSize(2)
        assertThat(
            response.body
                ?.data
                ?.betydninger
                ?.get("FOO"),
        ).contains(betydningFoo)
        assertThat(
            response.body
                ?.data
                ?.betydninger
                ?.get("BAR"),
        ).contains(betydningBar)
    }

    @Test
    fun `skal hente ut geografihierarki`() {
        val testData =
            requireNotNull(
                this::class.java.getResourceAsStream("/kodeverk/Geografirespons.json"),
            ) { "Missing test resource Geografirespons.json" }
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

        val dto: HierarkiGeografiInnlandDto =
            objectMapper.readValue(testData, HierarkiGeografiInnlandDto::class.java)

        val forventetNorge: LandDto = dto.norgeNode

        stubFor(
            get(GET_KODEVERK_GEOGRAFIHIERARKI_URL).willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(dto)),
            ),
        )

        val response: ResponseEntity<Ressurs<LandDto>> =
            restTemplate.exchange(
                localhost(KODEVERK_FYLKER_KOMMUNER_URL),
                HttpMethod.GET,
                null,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.data).isEqualTo(forventetNorge)
    }

    companion object {
        private const val KODEVERK_URL = "/api/kodeverk/"
        private const val KODEVERK_EEARG_URL = "$KODEVERK_URL/landkoder/eea"
        private const val GET_KODEVERK_EEAFREG_URL =
            "/api/v1/kodeverk/EEAFreg/koder/betydninger?ekskluderUgyldige=false&spraak=nb"
        private const val KODEVERK_FYLKER_KOMMUNER_URL = "$KODEVERK_URL/fylkerOgKommuner"
        private const val GET_KODEVERK_GEOGRAFIHIERARKI_URL = "/api/v1/hierarki/Geografi/noder?ekskluderUgyldige=true&spraak=nb"
    }
}
