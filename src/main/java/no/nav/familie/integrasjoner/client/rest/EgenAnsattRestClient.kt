package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.restklient.client.AbstractPingableRestClient
import no.nav.familie.restklient.util.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class EgenAnsattRestClient(
    @Value("\${EGEN_ANSATT_URL}") private val uri: URI,
    @Qualifier("noAuthorize") private val restTemplate: RestOperations,
) : AbstractPingableRestClient(restTemplate, "egenansatt") {
    override val pingUri: URI = UriUtil.uri(uri, PATH_PING)
    private val egenAnsattUri: URI = UriUtil.uri(uri, "skjermet")
    private val egenAnsattBulkUri: URI = UriUtil.uri(uri, "skjermetBulk")

    data class SkjermetDataRequestDTO(
        val personident: String,
    )

    data class SkjermetDataBolkRequestDTO(
        val personidenter: Set<String>,
    )

    fun erEgenAnsatt(personIdent: String): Boolean =
        try {
            postForEntity(egenAnsattUri, SkjermetDataRequestDTO(personIdent))
        } catch (e: Exception) {
            incrementLoggFeil("egenAnsatt.ident")
            throw e
        }

    fun erEgenAnsatt(personidenter: Set<String>): Map<String, Boolean> =
        try {
            postForEntity(egenAnsattBulkUri, SkjermetDataBolkRequestDTO(personidenter))
        } catch (e: Exception) {
            incrementLoggFeil("egenAnsatt.identer")
            throw e
        }

    companion object {
        private const val PATH_PING = "internal/health/readiness"
    }
}
