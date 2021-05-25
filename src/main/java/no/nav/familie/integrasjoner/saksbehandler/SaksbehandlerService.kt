package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(private val azureGraphRestClient: AzureGraphRestClient) {

    fun hentSaksbehandler(id: String): Saksbehandler {

        val azureAdBruker = azureGraphRestClient.hentSaksbehandler(id)

        return Saksbehandler(azureAdBruker.givenName,
                             azureAdBruker.surname)
    }

}
