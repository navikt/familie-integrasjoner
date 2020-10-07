package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import no.nav.familie.integrasjoner.dokarkiv.DokarkivController
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class EgenAnsattService(private val egenAnsattRestClient: EgenAnsattRestClient) {

    @Cacheable("erEgenAnsatt")
    fun erEgenAnsatt(fnr: String): Boolean = egenAnsattRestClient.erEgenAnsatt(fnr)
}