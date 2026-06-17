package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.felles.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class EgenAnsattRestClient(
    @Value("\${EGEN_ANSATT_URL}") private val uri: URI,
    @Qualifier("utenAuthHttpClient") private val restClient: RestClient,
) {
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
            restClient
                .post()
                .uri(egenAnsattUri)
                .body(SkjermetDataRequestDTO(personIdent))
                .retrieve()
                .body<Boolean>()!!
        } catch (e: Exception) {
            incrementLoggFeil("egenAnsatt.ident")
            throw e
        }

    fun erEgenAnsatt(personidenter: Set<String>): Map<String, Boolean> =
        try {
            restClient
                .post()
                .uri(egenAnsattBulkUri)
                .body(SkjermetDataBolkRequestDTO(personidenter))
                .retrieve()
                .body<Map<String, Boolean>>()!!
        } catch (e: Exception) {
            incrementLoggFeil("egenAnsatt.identer")
            throw e
        }
}
