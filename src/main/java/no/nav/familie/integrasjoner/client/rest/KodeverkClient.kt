package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.kontrakter.felles.kodeverk.HierarkiGeografiInnlandDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class KodeverkClient(
    @Value("\${KODEVERK_URL}") private val kodeverkUri: URI,
    @Value("\${KODEVERK_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    fun hentPostnummer(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("Postnummer"))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentPostnummerMedHistorikk(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("Postnummer", true))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentLandkoder(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("Landkoder"))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentLandkoderISO2(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("LandkoderISO2"))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentLandkoderMedHistorikk(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("Landkoder", true))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentEEALandkoder(): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri("EEAFreg", medHistorikk = true))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentKodeverk(kodeverksnavn: String): KodeverkDto =
        restClient
            .get()
            .uri(kodeverkUri(kodeverksnavn))
            .retrieve()
            .body<KodeverkDto>()!!

    fun hentGeografiInnland(): HierarkiGeografiInnlandDto =
        restClient
            .get()
            .uri(hierarkiUri("Geografi"))
            .retrieve()
            .body<HierarkiGeografiInnlandDto>()!!

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
        return UriUtil.uri(kodeverkUri, "api/v1/hierarki/$hierakinavn/noder", query)
    }

    companion object {
        private const val QUERY = "ekskluderUgyldige=true&spraak=nb"
        private const val QUERY_MED_HISTORIKK = "ekskluderUgyldige=false&spraak=nb"
    }
}
