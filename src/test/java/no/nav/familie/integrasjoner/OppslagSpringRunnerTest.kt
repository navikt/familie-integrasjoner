package no.nav.familie.integrasjoner

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.familie.integrasjoner.config.ApplicationConfig
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.After
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

@RunWith(SpringRunner::class)
@SpringBootTest(
        classes = [ApplicationConfig::class],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = [
            "no.nav.security.jwt.issuer.azuread.discoveryUrl=http://localhost:1234/azuread/.well-known/openid-configuration",
            "no.nav.security.jwt.issuer.azuread.accepted_audience=some-audience",
            "ENVIRONMENT_NAME: integrationtest"
        ],
)
@EnableMockOAuth2Server(port = 1234)
@TestInstance(Lifecycle.PER_CLASS)
abstract class OppslagSpringRunnerTest {

    protected var listAppender = initLoggingEventListAppender()
    protected var loggingEvents = listAppender.list
    protected var restTemplate = TestRestTemplate()
    protected var headers = HttpHeaders()

    @LocalServerPort
    private val port = 0

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @After fun reset() {
        loggingEvents.clear()
        headers.clear()
        mockOAuth2Server.shutdown()
    }

    protected fun getPort(): String {
        return port.toString()
    }

    protected fun localhost(uri: String): String {
        return LOCALHOST + getPort() + uri
    }

    protected fun url(baseUrl: String, uri: String): String {
        return baseUrl + uri
    }

    fun hentUrl(path: String) = "http://localhost:$port$path"

    fun token(claims: Map<String, Any> = emptyMap(),
              subject: String = DEFAULT_SUBJECT,
              audience: String = DEFAULT_AUDIENCE,
              issuerId: String = DEFAULT_ISSUER_ID): String {
        return mockOAuth2Server.issueToken(
                issuerId,
                "theclientid",
                DefaultOAuth2TokenCallback(
                        issuerId,
                        subject,
                        listOf(audience),
                        claims,
                        3600
                )
        ).serialize()
    }

    companion object {

        private const val LOCALHOST = "http://localhost:"
        private const val DEFAULT_ISSUER_ID = "azuread"
        private const val DEFAULT_SUBJECT = "subject"
        private const val DEFAULT_AUDIENCE = "some-audience"

        protected fun initLoggingEventListAppender(): ListAppender<ILoggingEvent> {
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            return listAppender
        }
    }
}