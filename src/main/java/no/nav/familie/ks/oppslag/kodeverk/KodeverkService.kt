package no.nav.familie.ef.mottak.api.kodeverk

import no.nav.familie.ks.oppslag.kodeverk.domene.Språk
import org.springframework.stereotype.Service

@Service
class KodeverkService(val kodeverkClient: KodeverkClient) {

    fun hentPoststedFor(postnummer: String): String {
        val postnummerBetydninger = kodeverkClient.hentPostnummerBetydninger()
        return postnummerBetydninger.betydninger[postnummer]?.get(0)?.beskrivelser?.get(Språk.BOKMÅL.kode)?.term ?: ""
    }
}
