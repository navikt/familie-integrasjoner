package no.nav.familie.integrasjoner.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics

val loggFeilCounter = mutableMapOf<String, Counter>()

fun incrementLoggFeil(
    kilde: String,
) {
    if (loggFeilCounter[kilde] == null) {
        loggFeilCounter[kilde] = Metrics.counter("logg.feil", "kilde", kilde)
    }

    loggFeilCounter[kilde]?.increment()
}