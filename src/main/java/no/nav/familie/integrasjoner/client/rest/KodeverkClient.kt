package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.kodeverk.domene.PostnummerDto
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

    val postnummerUri = UriUtil.uri(kodeverkUri, PATH_POSTNUMMER, QUERY_POSTNUMMER)

    fun hentPostnummerBetydninger(): PostnummerDto {
        return getForEntity(postnummerUri)
    }

    companion object {

        private const val PATH_PING = "internal/isAlive"
        private const val PATH_POSTNUMMER = "api/v1/kodeverk/Postnummer/koder/betydninger"
        private const val QUERY_POSTNUMMER = "ekskluderUgyldige=true&spraak=nb"
    }
}
