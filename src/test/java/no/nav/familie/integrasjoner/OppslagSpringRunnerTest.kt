package no.nav.familie.integrasjoner

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [UnitTestLauncher::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class OppslagSpringRunnerTest {

    protected val listAppender = initLoggingEventListAppender()
    protected val loggingEvents: MutableList<ILoggingEvent> = listAppender.list
    protected val restTemplate = TestRestTemplate()
    protected val headers = HttpHeaders()

    @LocalServerPort
    private val port = 0

    @AfterEach
    fun reset() {
        loggingEvents.clear()
        headers.clear()
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

    protected val lokalTestToken: String
        get() {
            val cookie = restTemplate.exchange(localhost("/local/cookie"), HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
            return tokenFraRespons(cookie)
        }

    private fun tokenFraRespons(cookie: ResponseEntity<String>): String {
        return cookie.body.split("value\":\"").toTypedArray()[1].split("\"").toTypedArray()[0]
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
