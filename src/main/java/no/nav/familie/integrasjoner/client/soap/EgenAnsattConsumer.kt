package no.nav.familie.integrasjoner.client.soap

import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.Pingable
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest

class EgenAnsattConsumer(private val egenAnsattV1: EgenAnsattV1) : Pingable {
    private val egenAnsattResponsTid =
            Metrics.timer("EgenAnsattV1.respons.tid")
    private val egenAnsattSuccess =
            Metrics.counter("EgenAnsattV1.response", "status", "success")
    private val egenAnsattFailure =
            Metrics.counter("EgenAnsattV1.response", "status", "failure")

    fun erEgenAnsatt(fnr: String?): Boolean {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                                                                               .withIdent(fnr)
        ).isEgenAnsatt
    }

    override fun ping() {
        egenAnsattV1.ping()
    }

}