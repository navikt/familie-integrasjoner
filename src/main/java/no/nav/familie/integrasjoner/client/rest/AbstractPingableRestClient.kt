package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import no.nav.familie.integrasjoner.client.Pingable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.http.*
import org.springframework.web.client.*
import java.net.URI
import java.util.concurrent.TimeUnit

abstract class AbstractPingableRestClient(operations: RestOperations,
                                          metricsPrefix: String) : AbstractRestClient(operations, metricsPrefix), Pingable {

    abstract val pingUri: URI

    override fun ping() {
        val response: ResponseEntity<String> = operations.getForEntity(pingUri)
        if (!response.statusCode.is2xxSuccessful) {
            throw HttpServerErrorException(response.statusCode)
        }
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}
