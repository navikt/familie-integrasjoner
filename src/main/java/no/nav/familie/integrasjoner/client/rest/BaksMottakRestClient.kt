package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class BaksMottakRestClient(
    @Value("\${FAMILIE_BAKS_MOTTAK_URL}") val mottakBaseUrl: URI,
    @Qualifier("jwtBearer") val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "baks-mottak") {
    fun hentPersonerIDigitalSøknad(
        tema: Tema,
        journalpostId: String,
    ): List<String> {
        val uri =
            UriComponentsBuilder
                .fromUri(mottakBaseUrl)
                .pathSegment("soknad/hent-personer-i-digital-soknad/$tema/$journalpostId")
                .build()
                .toUri()
        return getForEntity(uri)
    }
}
