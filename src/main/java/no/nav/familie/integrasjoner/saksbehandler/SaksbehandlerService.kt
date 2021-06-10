package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(private val azureGraphRestClient: AzureGraphRestClient) {

    private val lengdeNavIdent = 7

    fun hentSaksbehandler(id: String): Saksbehandler {

        val azureAdBruker = if (id.length == lengdeNavIdent) {
            val azureAdBrukere = azureGraphRestClient.finnSaksbehandler(id)

            if (azureAdBrukere.value.size != 1) {
                error("Feil ved søk. Oppslag på navIdent $id returnerte ${azureAdBrukere.value.size} forekomster.")
            }
            azureAdBrukere.value.first()

        } else {
            azureGraphRestClient.hentSaksbehandler(id)
        }

        return Saksbehandler(azureAdBruker.id,
                             azureAdBruker.onPremisesSamAccountName,
                             azureAdBruker.givenName,
                             azureAdBruker.surname)
    }
}
