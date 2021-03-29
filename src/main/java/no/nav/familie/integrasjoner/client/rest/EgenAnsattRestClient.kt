package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class EgenAnsattRestClient(@Value("\${EGEN_ANSATT_URL}") private val uri: URI,
                           @Qualifier("noAuthorize") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "egenansatt") {

    override val pingUri: URI = UriUtil.uri(uri, PATH_PING)
    private val egenAnsattUri: URI = UriUtil.uri(uri, "skjermet")

    data class SkjermetDataRequestDTO(val personident: String)

    fun erEgenAnsatt(fnr: String): Boolean = postForEntity(egenAnsattUri, SkjermetDataRequestDTO(fnr))

    companion object {

        private const val PATH_PING = "internal/health/readiness"
    }
}
