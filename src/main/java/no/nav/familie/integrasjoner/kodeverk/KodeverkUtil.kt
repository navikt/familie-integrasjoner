package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkSpråk
import no.nav.familie.kontrakter.felles.kodeverk.hentGjelende

fun KodeverkDto.mapTerm(): Map<String, String> {
    return betydninger.mapValues {
        if (it.value.isEmpty()) {
            ""
        } else if (it.value.size != 1) {
            this.hentGjelende(it.key) ?: ""
        } else {
            it.value.singleOrNull()?.beskrivelser?.get(KodeverkSpråk.BOKMÅL.kode)?.term ?: ""
        }
    }
}