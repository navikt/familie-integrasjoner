package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.http.*
import org.springframework.web.client.*
import java.net.URI
import java.util.concurrent.TimeUnit

abstract class AbstractRestClient(protected val operations: RestOperations,
                                  metricsPrefix: String) {

    protected val responstid: Timer = Metrics.timer("$metricsPrefix.tid")
    protected val responsSuccess: Counter = Metrics.counter("$metricsPrefix.response", "status", "success")
    protected val responsFailure: Counter = Metrics.counter("$metricsPrefix.response", "status", "failure")

    private val confidential: Marker = MarkerFactory.getMarker("CONFIDENTIAL")
    protected val log: Logger = LoggerFactory.getLogger(this::class.java)

    protected inline fun <reified T : Any> getForEntity(uri: URI): T {
        return executeMedMetrics(uri) { operations.getForEntity<T>(uri) } ?: error("Get feilet ved kall til $uri")
    }

    protected inline fun <reified T : Any> getForEntity(uri: URI, httpHeaders: HttpHeaders): T {
        return executeMedMetrics(uri) { operations.exchange<T>(uri, HttpMethod.GET, HttpEntity(null, httpHeaders)) }
               ?: error("Get feilet ved kall til $uri")
    }

    protected inline fun <reified T : Any> postForEntity(uri: URI, payload: Any): T? {
        return executeMedMetrics(uri) { operations.postForEntity<T>(uri, payload) }
    }

    protected inline fun <reified T : Any> postForEntity(uri: URI, payload: Any, httpHeaders: HttpHeaders): T? {
        return executeMedMetrics(uri) { operations.exchange<T>(uri, HttpMethod.POST, HttpEntity(payload, httpHeaders)) }
    }

    protected inline fun <reified T : Any> putForEntity(uri: URI, payload: Any): T? {
        return executeMedMetrics(uri) { operations.exchange<T>(RequestEntity.put(uri).body(payload)) }
    }

    protected inline fun <reified T : Any> putForEntity(uri: URI, payload: Any, httpHeaders: HttpHeaders): T? {
        return executeMedMetrics(uri) { operations.exchange<T>(uri, HttpMethod.PUT, HttpEntity(payload, httpHeaders)) }
    }

    protected inline fun <reified T : Any> patchForEntity(uri: URI, payload: Any): T? {
        return executeMedMetrics(uri) { operations.exchange<T>(RequestEntity.patch(uri).body(payload)) }
    }

    private fun <T> validerOgPakkUt(respons: ResponseEntity<T>, uri: URI): T? {
        if (!respons.statusCode.is2xxSuccessful) {
            log.info(confidential, "Kall mot $uri feilet:  ${respons.body}")
            log.info("Kall mot $uri feilet: ${respons.statusCode}")
            throw HttpServerErrorException(respons.statusCode, "",  respons.body?.toString()?.toByteArray(), Charsets.UTF_8)
        }
        return respons.body
    }

    protected fun <T> executeMedMetrics(uri: URI, function: () -> ResponseEntity<T>): T? {
        try {
            val startTime = System.nanoTime()
            val responseEntity = function.invoke()
            responstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            responsSuccess.increment()
            return validerOgPakkUt(responseEntity, uri)
        } catch (e: RestClientResponseException) {
            responsFailure.increment()
            throw e
        } catch (e: Exception) {
            responsFailure.increment()
            throw RuntimeException("Feil ved kall mot uri=$uri", e)
        }
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}
