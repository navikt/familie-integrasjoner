package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI


@Component
class EgenAnsattRestClient(@Value("\${EGEN_ANSATT_URL}") private val egenAnsattUri: URI,
                           @Qualifier("noAuthorize") private val restTemplate: RestOperations)
    : AbstractRestClient(restTemplate, "egenansatt") {

    fun erEgenAnsatt(fnr: String): Boolean = getForEntity(egenAnsattUri(fnr))
    private fun egenAnsattUri(fnr: String): URI = UriUtil.uri(egenAnsattUri, "skjermet", "personident=$fnr")
}
