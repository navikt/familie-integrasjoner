package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KodeverkClient(@Value("\${KODEVERK_URL}") private val kodeverkUri: URI,
                     @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "kodeverk") {

    override val pingUri: URI = UriUtil.uri(kodeverkUri, PATH_PING)

    fun hentPostnummer(): KodeverkDto {
        return getForEntity(kodeverkUri("Postnummer"))
    }

    fun hentPostnummerMedHistorikk(): KodeverkDto {
        return getForEntity(kodeverkUri("Postnummer", true))
    }

    fun hentLandkoder(): KodeverkDto {
        return getForEntity(kodeverkUri("Landkoder"))
    }

    fun hentLandkoderMedHistorikk(): KodeverkDto {
        return getForEntity(kodeverkUri("Landkoder", true))
    }

    private fun kodeverkUri(kodeverksnavn: String,
                            medHistorikk: Boolean = false): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return UriUtil.uri(kodeverkUri, "api/v1/kodeverk/$kodeverksnavn/koder/betydninger", query)
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }
}
