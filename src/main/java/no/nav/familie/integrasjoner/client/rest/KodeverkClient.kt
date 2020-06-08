package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KodeverkClient(@Value("\${KODEVERK_URL}") private val kodeverkUri: URI,
                     @Qualifier("sts") val restTemplate: RestOperations,
                     private val stsRestClient: StsRestClient)
    : AbstractPingableRestClient(restTemplate, "kodeverk") {

    override val pingUri: URI = UriUtil.uri(kodeverkUri, PATH_PING)

    fun hentPostnummer(): KodeverkDto {
        return getForEntity(kodeverkUri("Postnummer"))
    }

    fun hentPostnummerMedHistorikk(): KodeverkDto {
        return getForEntity(UriUtil.uri(kodeverkUri, "Postnummer", QUERY_MED_HISTORIKK))
    }

    fun hentLandkoder(): KodeverkDto {
        return getForEntity(kodeverkUri("Landkoder"))
    }

    fun hentLandkoderMedHistorikk(): KodeverkDto {
        return getForEntity(UriUtil.uri(kodeverkUri, "Landkoder", QUERY_MED_HISTORIKK), httpHeaders())
    }

    private fun kodeverkUri(kodeverksnavn: String,
                            medHistorikk: Boolean = false): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return UriUtil.uri(kodeverkUri, "api/v1/kodeverk/$kodeverksnavn/koder/betydninger", query)
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
        }
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }
}
