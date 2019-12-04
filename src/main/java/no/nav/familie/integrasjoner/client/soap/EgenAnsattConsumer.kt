package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.integrasjoner.client.Pingable
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.springframework.stereotype.Component

@Component
class EgenAnsattConsumer(private val egenAnsattV1: EgenAnsattV1) : AbstractSoapClient("EgenAnsattV1"), Pingable {

    fun erEgenAnsatt(fnr: String?): Boolean {
        return executeMedMetrics {
            egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                                                                            .withIdent(fnr))
        }.isEgenAnsatt
    }

    override fun ping() {
        egenAnsattV1.ping()
    }

}