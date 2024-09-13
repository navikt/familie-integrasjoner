package no.nav.familie.integrasjoner.axsys

import no.nav.familie.integrasjoner.client.rest.AxsysRestClient
import no.nav.familie.kontrakter.felles.enhet.Enhet
import org.springframework.stereotype.Service

@Service
class AxsysService(
    private val axsysRestClient: AxsysRestClient,
) {
    fun hentEnheterNavIdentHarTilgangTil(navIdent: NavIdent): List<Enhet> = axsysRestClient.hentEnheterNavIdentHarTilgangTil(navIdent).enheter.map { Enhet(it.enhetId) }
}
