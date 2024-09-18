package no.nav.familie.integrasjoner.axsys

import no.nav.familie.integrasjoner.client.rest.AxsysRestClient
import no.nav.familie.kontrakter.felles.NavIdent
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.enhet.Enhet
import org.springframework.stereotype.Service

@Service
class AxsysService(
    private val axsysRestClient: AxsysRestClient,
) {
    fun hentEnheterNavIdentHarTilgangTil(
        navIdent: NavIdent,
        tema: Tema,
    ): List<Enhet> =
        axsysRestClient
            .hentEnheterNavIdentHarTilgangTil(navIdent)
            .enheter
            .filter { it.temaer.contains(tema.name) }
            .map { Enhet(it.enhetId, it.navn) }
}
