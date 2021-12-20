package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI


@Service
class ArbeidsfordelingRestClient(@Value("\${NORG2_URL}")
                                 private val norg2Uri: URI,
                                 @Qualifier("noAuthorize")
                                 restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "norg2") {

    fun hentEnhet(geografiskOmråde: String): NavKontorEnhet {
        return getForEntity(UriComponentsBuilder.fromUri(norg2Uri)
                                    .pathSegment("api/v1/enhet/navkontor/$geografiskOmråde")
                                    .build()
                                    .toUri())
    }

    fun hentNavkontor(enhetId: String): NavKontorEnhet {
        return getForEntity(UriComponentsBuilder.fromUri(norg2Uri)
                                    .pathSegment("api/v1/enhet/$enhetId")
                                    .build()
                                    .toUri())
    }

    fun finnBehandlendeEnhetMedBesteMatch(gjeldendeTema: no.nav.familie.kontrakter.felles.Tema,
                                          gjeldendeGeografiskOmråde: String?,
                                          gjeldendeDiskresjonskode: String?,
                                          erEgenAnsatt: Boolean): List<Enhet> {
        return postForEntity<List<NavKontorEnhet>>(
                UriComponentsBuilder.fromUri(norg2Uri)
                        .pathSegment("api/v1/arbeidsfordeling/enheter/bestmatch")
                        .build()
                        .toUri(),
                ArbeidsfordelingKritierie(diskresjonskode = gjeldendeDiskresjonskode,
                                          geografiskOmraade = gjeldendeGeografiskOmråde,
                                          tema = gjeldendeTema.name,
                                          skjermet = erEgenAnsatt),

                ).map { Enhet(enhetId = it.enhetNr, enhetNavn = it.navn) }
    }

    override val pingUri: URI
        get() = UriComponentsBuilder.fromUri(norg2Uri).pathSegment("api/ping").build().toUri()

}
