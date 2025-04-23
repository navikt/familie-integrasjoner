package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.azure.domene.AzureAdSaksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SaksbehandlerService(
    private val azureGraphRestClient: AzureGraphRestClient,
    private val environment: Environment,
) {
    private val lengdeNavIdent = 7

    fun hentSaksbehandler(id: String): AzureAdSaksbehandler {
        // TODO: Midlertidig for å få ut funksjonalitet. Fjern når ba-sak-e2e fases ut.
        if (environment.activeProfiles.any { it == "e2e" }) {
            return AzureAdSaksbehandler(
                azureId = UUID.randomUUID(),
                navIdent = id,
                fornavn = "Mocka",
                etternavn = "Saksbehandler",
                enhet = "4408",
                geografiskEnhet = "Skien"
            )
        }

        if (id == ID_VEDTAKSLØSNINGEN) {
            return AzureAdSaksbehandler(
                UUID.randomUUID(),
                ID_VEDTAKSLØSNINGEN,
                "Vedtaksløsning",
                "Nav",
                "9999",
                "Skien"
            )
        }

        val azureAdBruker =
            if (id.length == lengdeNavIdent) {
                val azureAdBrukere = azureGraphRestClient.finnSaksbehandler(id)

                if (azureAdBrukere.value.size != 1) {
                    error("Feil ved søk. Oppslag på navIdent $id returnerte ${azureAdBrukere.value.size} forekomster.")
                }
                azureAdBrukere.value.first()
            } else {
                azureGraphRestClient.hentSaksbehandler(id)
            }

        return AzureAdSaksbehandler(
            azureAdBruker.id,
            azureAdBruker.onPremisesSamAccountName,
            azureAdBruker.givenName,
            azureAdBruker.surname,
            azureAdBruker.streetAddress,
            azureAdBruker.city,
        )
    }

    fun hentNavIdent(saksbehandlerId: String): String = saksbehandlerId.takeIf { it.length == lengdeNavIdent } ?: hentSaksbehandler(saksbehandlerId).navIdent

    companion object {
        const val ID_VEDTAKSLØSNINGEN = "VL"
    }
}
