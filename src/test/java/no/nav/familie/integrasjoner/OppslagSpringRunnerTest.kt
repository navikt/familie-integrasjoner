package no.nav.familie.integrasjoner

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cache.CacheManager
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@EnableMockOAuth2Server
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [UnitTestLauncher::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class OppslagSpringRunnerTest {
    protected val listAppender = initLoggingEventListAppender()
    protected val loggingEvents: MutableList<ILoggingEvent> = listAppender.list
    protected val restTemplate = TestRestTemplate()
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @LocalServerPort
    private val port = 0

    @AfterEach
    fun reset() {
        loggingEvents.clear()
        headers.clear()
        clearCaches()
    }

    private fun clearCaches() {
        listOf(cacheManager).forEach {
            it.cacheNames
                .mapNotNull { cacheName -> it.getCache(cacheName) }
                .forEach { cache -> cache.clear() }
        }
    }

    protected fun getPort(): String = port.toString()

    protected fun localhost(uri: String): String = LOCALHOST + getPort() + uri

    protected fun url(
        baseUrl: String,
        uri: String,
    ): String = baseUrl + uri

    protected val lokalTestToken: String
        get() {
            return lagToken()
        }

    fun lagToken(saksbehandler: String = "testbruker"): String {
        val clientId = UUID.randomUUID().toString()
        val brukerId = UUID.randomUUID().toString()
        val issuerId = "azuread"
        return mockOAuth2Server
            .issueToken(
                issuerId = issuerId,
                clientId = clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = issuerId,
                    subject = brukerId,
                    audience = listOf("aud-localhost"),
                    claims =
                        mapOf(
                            "oid" to brukerId,
                            "azp" to clientId,
                            "name" to saksbehandler,
                            "NAVident" to saksbehandler,
                            "groups" to emptyList<String>(),
                        ),
                    expiry = 3600,
                ),
            ).serialize()
    }

    companion object {
        const val MOCK_SERVER_PORT = 18321
        private const val LOCALHOST = "http://localhost:"

        protected fun initLoggingEventListAppender(): ListAppender<ILoggingEvent> {
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            return listAppender
        }
    }
}
