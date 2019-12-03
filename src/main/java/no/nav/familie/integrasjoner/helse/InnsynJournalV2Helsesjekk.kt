package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.soap.InnsynJournalConsumer
import org.springframework.stereotype.Component

@Component
internal class InnsynJournalV2Helsesjekk(innsynJournalV2: InnsynJournalConsumer) : AbstractHealthIndicator(innsynJournalV2) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.innsynJournalV2", "status", "nede")
}