package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class EgenAnsattService(private val egenAnsattRestClient: EgenAnsattRestClient) {

    @Cacheable("erEgenAnsatt")
    fun erEgenAnsatt(fnr: String): Boolean {
        LOG.info("Er egen ansatt")
        val erEgenAnsatt = egenAnsattRestClient.erEgenAnsatt(fnr)
        LOG.info("Er egenansatt: $erEgenAnsatt")
        return egenAnsattRestClient.erEgenAnsatt(fnr)
    }
    companion object {
        private val LOG = LoggerFactory.getLogger(EgenAnsattService::class.java)
    }
}