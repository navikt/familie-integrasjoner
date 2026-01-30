package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Status.SUKSESS
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.oppgave.Behandlingstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.resttestclient.postForObject
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integrasjonstest")
class ArbeidsfordelingControllerTest : OppslagSpringRunnerTest() {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun arbeidsfordelingService(): ArbeidsfordelingService = mockk(relaxed = true)
    }

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Nested
    inner class HentBehandlendeEnhetForPersonIdentV2 {
        @Test
        fun `skal returnere enheter med behandlingstype-parameter`() {
            val arbeidsfordelingService = applicationContext.getBean<ArbeidsfordelingService>()
            val enhet = Enhet(enhetId = "4817", enhetNavn = "Nav familie- og pensjonsytelser Steinkjer")
            every { arbeidsfordelingService.finnBehandlendeEnhetForPerson(IDENT, Tema.KON, any()) } returns listOf(enhet)

            val response =
                restTemplate.postForObject<Ressurs<List<Map<*, *>>>>(
                    localhost("$BASE_PATH/enhet/${Tema.KON}?behandlingstype=${Behandlingstype.NASJONAL}"),
                    HttpEntity(PersonIdent(IDENT), headers),
                )

            assertThat(response!!.status).isEqualTo(SUKSESS)
            assertThat(response.data).isNotNull().hasSize(1)

            val data = response.data!!.single()
            assertThat(data["enhetId"]).isEqualTo("4817")
            assertThat(data["enhetNavn"]).isEqualTo("Nav familie- og pensjonsytelser Steinkjer")

            verify { arbeidsfordelingService.finnBehandlendeEnhetForPerson(IDENT, Tema.KON, Behandlingstype.NASJONAL) }
        }

        @Test
        fun `skal returnere enheter uten behandlingstype-parameter`() {
            val arbeidsfordelingService = applicationContext.getBean<ArbeidsfordelingService>()
            val enhet = Enhet(enhetId = "4817", enhetNavn = "Nav familie- og pensjonsytelser Steinkjer")
            every { arbeidsfordelingService.finnBehandlendeEnhetForPerson(IDENT, Tema.KON, any()) } returns listOf(enhet)

            val response =
                restTemplate.postForObject<Ressurs<List<Map<*, *>>>>(
                    localhost("$BASE_PATH/enhet/${Tema.KON}"),
                    HttpEntity(PersonIdent(IDENT), headers),
                )

            assertThat(response!!.status).isEqualTo(SUKSESS)
            assertThat(response.data).isNotNull().hasSize(1)

            val data = response.data!!.single()
            assertThat(data["enhetId"]).isEqualTo("4817")
            assertThat(data["enhetNavn"]).isEqualTo("Nav familie- og pensjonsytelser Steinkjer")

            verify { arbeidsfordelingService.finnBehandlendeEnhetForPerson(IDENT, Tema.KON, null) }
        }
    }

    companion object {
        private const val BASE_PATH = "/api/arbeidsfordeling"
        private const val IDENT = "12345678910"
    }
}
