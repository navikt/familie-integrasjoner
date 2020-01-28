package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.soap.InnsynJournalSoapClient
import org.springframework.stereotype.Component

@Component
internal class InnsynJournalV2Helsesjekk(innsynJournalV2: InnsynJournalSoapClient)
    : AbstractHealthIndicator(innsynJournalV2, "helsesjekk.innsynJournalV2")
