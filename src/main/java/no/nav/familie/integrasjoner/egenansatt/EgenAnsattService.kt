package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class EgenAnsattService(private val egenAnsattRestClient: EgenAnsattRestClient) {
    @Cacheable("erEgenAnsatt")
    fun erEgenAnsatt(personIdent: String): Boolean = egenAnsattRestClient.erEgenAnsatt(personIdent)

    fun erEgenAnsatt(personIdenter: Set<String>): Map<String, Boolean> = egenAnsattRestClient.erEgenAnsatt(personIdenter)
}
