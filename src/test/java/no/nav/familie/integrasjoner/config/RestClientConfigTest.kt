package no.nav.familie.integrasjoner.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class RestClientConfigTest {
    private val factory: EntraIDRestClientFactory = mockk()
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(RestClientConfig::class.java, EgenAnsattRestClient::class.java)
            .withBean(EntraIDRestClientFactory::class.java, { factory })
            .withBean(TokenXClient::class.java, { mockk() })
            .withBean(ConsumerIdClientInterceptor::class.java, { mockk() })
            .withBean(MdcValuesPropagatingClientInterceptor::class.java, { mockk() })
            .withPropertyValues(
                "SAF_TOKENX_SCOPE=test-scope",
            )

    @ParameterizedTest
    @ValueSource(strings = ["gcp", "preprod,preprod-gcp,gcp", "prod,prod-gcp,gcp"])
    fun `skal bruke maskin-til-maskin-klienten for begge skjermingskall med gcp-profil`(profiles: String) {
        val scope = "api://dev-gcp.nom.skjermede-personer-pip/.default"
        val builder =
            RestClient
                .builder()
                .requestInterceptor { request, body, execution ->
                    request.headers.setBearerAuth("test-token")
                    execution.execute(request, body)
                }
        val server = MockRestServiceServer.bindTo(builder).build()
        every { factory.lagMaskinTilMaskinRestKlient(scope) } returns builder.build()

        server
            .expect(requestTo("http://skjermede-personer-pip/skjermet"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
            .andRespond(withSuccess("true", MediaType.APPLICATION_JSON))
        server
            .expect(requestTo("http://skjermede-personer-pip/skjermetBulk"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
            .andRespond(withSuccess("""{"12345678910":false}""", MediaType.APPLICATION_JSON))

        contextRunner
            .withPropertyValues(
                "spring.profiles.active=$profiles",
                "EGEN_ANSATT_SCOPE=$scope",
                "EGEN_ANSATT_URL=http://skjermede-personer-pip",
            )
            .run { context ->
                assertThat(context).hasNotFailed()
                val client = context.getBean(EgenAnsattRestClient::class.java)

                assertThat(client.erEgenAnsatt("12345678910")).isTrue()
                assertThat(client.erEgenAnsatt(setOf("12345678910"))).containsEntry("12345678910", false)
                verify(exactly = 1) { factory.lagMaskinTilMaskinRestKlient(scope) }
                server.verify()
            }
    }

    @Test
    fun `skal laste prod-gcp-konfigurasjon og bruke prod-scope mot skjerming i gcp`() {
        val scope = "api://prod-gcp.nom.skjermede-personer-pip/.default"
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        every { factory.lagMaskinTilMaskinRestKlient(scope) } returns builder.build()

        server
            .expect(requestTo("http://skjermede-personer-pip.nom/skjermet"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("true", MediaType.APPLICATION_JSON))

        contextRunner
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withPropertyValues("spring.profiles.active=prod,prod-gcp,gcp")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(EgenAnsattRestClient::class.java).erEgenAnsatt("12345678910")).isTrue()
                verify(exactly = 1) { factory.lagMaskinTilMaskinRestKlient(scope) }
                server.verify()
            }
    }

    @ParameterizedTest
    @ValueSource(strings = ["prod", "preprod", "integrasjonstest", "dev"])
    fun `skal beholde klient uten autentisering uten gcp-profil`(profile: String) {
        contextRunner
            .withPropertyValues("spring.profiles.active=$profile", "EGEN_ANSATT_URL=http://skjermede-personer-pip")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean("egenAnsattHttpClient")).isSameAs(context.getBean("utenAuthHttpClient"))
                verify(exactly = 0) { factory.lagMaskinTilMaskinRestKlient(any()) }
            }
    }
}
