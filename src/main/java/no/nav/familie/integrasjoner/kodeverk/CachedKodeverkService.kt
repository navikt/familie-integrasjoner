package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

// TODO ta i bruk fra kontrakter
typealias InntektKodeverkDto = Map<InntektKodeverkType, Map<String, String>>
enum class InntektKodeverkType(val kodeverk: String) {
    LOENNSINNTEKT("Loennsbeskrivelse"),
    NAERINGSINNTEKT("Naeringsinntektsbeskrivelse"),
    PENSJON_ELLER_TRYGD("PensjonEllerTrygdeBeskrivelse"),
    YTELSE_FRA_OFFENTLIGE("YtelseFraOffentligeBeskrivelse"),
    TILLEGSINFORMASJON_KATEGORI("EDAGTilleggsinfoKategorier")
}


@Service
class CachedKodeverkService(private val kodeverkClient: KodeverkClient) {

    @Cacheable("kodeverk_postestedMedHistorikk", sync = true)
    fun hentPostnummerMedHistorikk(): KodeverkDto = kodeverkClient.hentPostnummerMedHistorikk()

    @Cacheable("kodeverk_postested")
    fun hentPostnummer(): Map<String, String> = kodeverkClient.hentPostnummer().mapTerm()

    @Cacheable("kodeverk_landkoder")
    fun hentLandkoder(): Map<String, String> = kodeverkClient.hentLandkoder().mapTerm()

    @Cacheable("kodeverk_landkoderMedHistorikk")
    fun hentLandkoderMedHistorikk(): KodeverkDto = kodeverkClient.hentLandkoderMedHistorikk()

    @Cacheable("kodeverk_eeafregMedHistorikk")
    fun hentEEALandkoder(): KodeverkDto = kodeverkClient.hentEEALandkoder()

    @Cacheable("inntekt")
    fun hentInntekt(): InntektKodeverkDto {
        return InntektKodeverkType.values().associateWith {
            kodeverkClient.hentKodeverk(it.kodeverk).mapTerm()
        }
    }

}

