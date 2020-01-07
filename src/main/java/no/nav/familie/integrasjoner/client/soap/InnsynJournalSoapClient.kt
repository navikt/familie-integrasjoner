package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.integrasjoner.client.Pingable
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostObjektIkkeFunnet
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class InnsynJournalSoapClient(private val port: InnsynJournalV2) : AbstractSoapClient("InnsynJournalV2"), Pingable {

    private val logger = LoggerFactory.getLogger(InnsynJournalSoapClient::class.java)

    fun hentJournalpost(referanseId: String): IdentifiserJournalpostResponse? {
        val request = IdentifiserJournalpostRequest().apply {
            kanalReferanseId = referanseId
        }
        return try {
            executeMedMetrics { port.identifiserJournalpost(request) }
        } catch (e: IdentifiserJournalpostObjektIkkeFunnet) {
            logger.info("Fant ikke journalpost med kanalReferanseId={}", referanseId)
            null
        } catch (e: Exception) {
            throw RuntimeException("Innsyn klarte ikke å hente journalpost med kanalReferanseId=$referanseId", e)
        }
    }

    override fun ping() {
        port.ping()
    }
}