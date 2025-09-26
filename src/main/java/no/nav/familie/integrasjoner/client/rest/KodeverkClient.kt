package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.kontrakter.felles.kodeverk.HierarkiGeografiInnlandDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KodeverkClient(
    @Value("\${KODEVERK_URL}") private val kodeverkUri: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "kodeverk") {
    fun hentPostnummer(): KodeverkDto = getForEntity(kodeverkUri("Postnummer"))

    fun hentPostnummerMedHistorikk(): KodeverkDto = getForEntity(kodeverkUri("Postnummer", true))

    fun hentLandkoder(): KodeverkDto = getForEntity(kodeverkUri("Landkoder"))

    fun hentLandkoderISO2(): KodeverkDto = getForEntity(kodeverkUri("LandkoderISO2"))

    fun hentLandkoderMedHistorikk(): KodeverkDto = getForEntity(kodeverkUri("Landkoder", true))

    fun hentEEALandkoder(): KodeverkDto = getForEntity(kodeverkUri("EEAFreg", medHistorikk = true))

    fun hentKodeverk(kodeverksnavn: String): KodeverkDto = getForEntity(kodeverkUri(kodeverksnavn))

    fun hentGeografiInnland(): HierarkiGeografiInnlandDto = getForEntity(hierarkiUri("Geografi"))

    private fun kodeverkUri(
        kodeverksnavn: String,
        medHistorikk: Boolean = false,
    ): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return UriUtil.uri(kodeverkUri, "api/v1/kodeverk/$kodeverksnavn/koder/betydninger", query)
    }

    private fun hierarkiUri(
        hierakinavn: String,
        medHistorikk: Boolean = false,
    ): URI {
        val query = if (medHistorikk) QUERY_MED_HISTORIKK else QUERY
        return UriUtil.uri(kodeverkUri, "api/v1/hierarki/$hierakinavn/koder/noder", query)
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }
}
