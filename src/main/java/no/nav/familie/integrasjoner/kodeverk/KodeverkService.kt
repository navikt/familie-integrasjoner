package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.integrasjoner.kodeverk.domene.Språk
import org.springframework.stereotype.Service

@Service
class KodeverkService(val kodeverkClient: KodeverkClient) {

    fun hentPoststedFor(postnummer: String): String {
        val postnummerBetydninger = kodeverkClient.hentPostnummerBetydninger()
        return postnummerBetydninger.betydninger[postnummer]?.get(0)?.beskrivelser?.get(Språk.BOKMÅL.kode)?.term ?: ""
    }
}
