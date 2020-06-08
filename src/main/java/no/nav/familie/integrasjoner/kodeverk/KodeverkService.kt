package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import no.nav.familie.integrasjoner.kodeverk.domene.mapTerm
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class KodeverkService(val kodeverkClient: KodeverkClient) {

    fun hentPoststed(postnummer: String): String {
        return hentPostnummer().getOrDefault(postnummer, "")
    }

    @Cacheable("kodeverk_postestedMedHistorikk")
    fun hentPostnummerMedHistorikk(): KodeverkDto = kodeverkClient.hentPostnummerMedHistorikk()

    @Cacheable("kodeverk_postested")
    protected fun hentPostnummer(): Map<String, String> = kodeverkClient.hentPostnummer().mapTerm()

    fun hentLandkode(landkode: String): String = hentLandkoder().getOrDefault(landkode, "")

    @Cacheable("kodeverk_landkoder")
    fun hentLandkoder(): Map<String, String> = kodeverkClient.hentLandkoder().mapTerm()

    @Cacheable("kodeverk_landkoderMedHistorikk")
    fun hentLandkoderMedHistorikk(): KodeverkDto = kodeverkClient.hentLandkoderMedHistorikk()
}
