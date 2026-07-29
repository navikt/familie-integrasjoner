package no.nav.familie.integrasjoner.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC

val loggFeilCounter = mutableMapOf<String, Counter>()

private val IGNORERTE_KILDER_FOR_TILBAKE = setOf("oppgave.opprettOppgave", "oppgave.oppdaterOppgave")

fun incrementLoggFeil(
    kilde: String,
) {
    // Ignorer kjente feil fra familie-tilbake for å ikke trigge alarmer
    if (MDC.get(MDCConstants.MDC_CONSUMER_ID) == "familie-tilbake" && kilde in IGNORERTE_KILDER_FOR_TILBAKE) {
        return
    }

    if (loggFeilCounter[kilde] == null) {
        loggFeilCounter[kilde] = Metrics.counter("logg.feil", "kilde", kilde)
    }

    loggFeilCounter[kilde]?.increment()
}
