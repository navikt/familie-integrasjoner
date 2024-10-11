package no.nav.familie.integrasjoner.mottak

import no.nav.familie.integrasjoner.client.rest.MottakRestClient
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.stereotype.Service

@Service
class MottakService(
    private val mottakRestClient: MottakRestClient,
) {
    fun hentPersonerIDigitalSøknad(
        tema: Tema,
        journalpostId: String,
    ): List<String> = mottakRestClient.hentPersonerIDigitalSøknad(tema, journalpostId)
}
