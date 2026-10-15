package no.nav.familie.integrasjoner.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.util.LogbackMDCAdapter
import ch.qos.logback.core.ConsoleAppender
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class LogbackStdoutStacktraceTest {
    private val maksLinjelengdeBytes = 16 * 1024
    private lateinit var loggerContext: LoggerContext

    @BeforeEach
    fun setup() {
        loggerContext = LoggerContext()
        loggerContext.mdcAdapter = LogbackMDCAdapter()
        JoranConfigurator().apply {
            context = loggerContext
            doConfigure(javaClass.classLoader.getResource("logback-spring.xml"))
        }
    }

    @AfterEach
    fun tearDown() {
        loggerContext.stop()
    }

    @Test
    fun `logglinje med dyp stacktrace og flere Caused by holder seg under 16 KB`() {
        val linje = enkod(lagLoggEvent(storExceptionOver16KB()))

        assertThat(linje.size).isLessThan(maksLinjelengdeBytes)
    }

    @Test
    fun `logglinje med forkortet stacktrace er gyldig JSON med rotårsaken først`() {
        val linje = enkod(lagLoggEvent(storExceptionOver16KB()))

        val json = jsonMapper.readTree(linje)
        assertThat(json.get("message").asString()).isEqualTo("Request processing failed")
        assertThat(json.get("stack_trace").asString()).startsWith("java.io.IOException: rotårsak")
    }

    private fun enkod(event: ILoggingEvent): ByteArray {
        @Suppress("UNCHECKED_CAST")
        val appender =
            loggerContext.getLogger("ROOT").getAppender("stdout_json") as ConsoleAppender<ILoggingEvent>
        return appender.encoder.encode(event)
    }

    private fun lagLoggEvent(e: Throwable): LoggingEvent =
        LoggingEvent(
            "no.nav.familie.log.filter.LogFilter",
            loggerContext.getLogger("no.nav.familie.log.filter.LogFilter"),
            Level.ERROR,
            "Request processing failed",
            e,
            null,
        )

    private fun storExceptionOver16KB(): Throwable {
        val rot = IOException("rotårsak").medRammer("org.eclipse.jetty.server.Handler", 150)
        val midt = IllegalStateException("midt", rot).medRammer("org.springframework.web.filter.Filter", 150)
        return RuntimeException("topp", midt).medRammer("no.nav.familie.integrasjoner.Klient", 150)
    }

    private fun <T : Throwable> T.medRammer(
        klasse: String,
        antall: Int,
    ): T =
        apply {
            stackTrace = Array(antall) { StackTraceElement("$klasse$it", "metode$it", "Fil$it.java", it + 1) }
        }
}
