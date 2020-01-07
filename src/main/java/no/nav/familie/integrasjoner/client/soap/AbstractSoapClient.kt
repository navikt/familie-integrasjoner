package no.nav.familie.integrasjoner.client.soap

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit

abstract class AbstractSoapClient(metricsPrefix: String) {

    protected val responstid: Timer = Metrics.timer("$metricsPrefix.tid")
    protected val responsSuccess: Counter = Metrics.counter("$metricsPrefix.response", "status", "success")
    protected val responsFailure: Counter = Metrics.counter("$metricsPrefix.response", "status", "failure")

    protected inline fun <reified T> executeMedMetrics(function: () -> T): T {
        try {
            val startTime = System.nanoTime()
            val entity = function.invoke()
            responstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            responsSuccess.increment()
            return entity
        } catch (e: Exception) {
            responsFailure.increment()
            throw e
        }
    }


}