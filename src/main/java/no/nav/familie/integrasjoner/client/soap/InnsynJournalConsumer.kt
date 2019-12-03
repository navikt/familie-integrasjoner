package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.integrasjoner.client.Pingable
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.*
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse
import org.slf4j.LoggerFactory

class InnsynJournalConsumer(private val port: InnsynJournalV2): Pingable {
    fun hentJournalpost(kanalReferanseId: String): IdentifiserJournalpostResponse? {
        val request = IdentifiserJournalpostRequest()
        request.kanalReferanseId = kanalReferanseId
        return try {
            port.identifiserJournalpost(request)
        } catch (e: IdentifiserJournalpostJournalpostIkkeInngaaende) {
            throw RuntimeException("Innsyn klarte ikke å hente journalpost med kanalReferanseId=$kanalReferanseId", e)
        } catch (e: IdentifiserJournalpostUgyldingInput) {
            throw RuntimeException("Innsyn klarte ikke å hente journalpost med kanalReferanseId=$kanalReferanseId", e)
        } catch (e: IdentifiserJournalpostUgyldigAntallJournalposter) {
            throw RuntimeException("Innsyn klarte ikke å hente journalpost med kanalReferanseId=$kanalReferanseId", e)
        } catch (e: IdentifiserJournalpostObjektIkkeFunnet) {
            LOG.info("Fant ikke journalpost med kanalReferanseId={}",
                     kanalReferanseId)
            null
        }
    }

    override fun ping() {
        port.ping()
    }

    companion object {
        private val LOG =
                LoggerFactory.getLogger(InnsynJournalConsumer::class.java)
    }

}