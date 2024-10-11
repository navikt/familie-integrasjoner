package no.nav.familie.integrasjoner.mottak

import no.nav.familie.integrasjoner.client.rest.BaksMottakRestClient
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.stereotype.Service

@Service
class BaksMottakService(
    private val baksMottakRestClient: BaksMottakRestClient,
) {
    fun hentPersonerIDigitalSøknad(
        tema: Tema,
        journalpostId: String,
    ): List<String> = baksMottakRestClient.hentPersonerIDigitalSøknad(tema, journalpostId)
}
