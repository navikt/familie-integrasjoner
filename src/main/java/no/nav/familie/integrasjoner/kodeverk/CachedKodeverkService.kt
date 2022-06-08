package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.InntektKodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.InntektKodeverkType
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

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
