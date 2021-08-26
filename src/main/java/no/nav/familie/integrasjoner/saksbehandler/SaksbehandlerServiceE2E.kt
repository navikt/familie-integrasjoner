package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Profile("e2e")
class SaksbehandlerServiceE2E(private val azureGraphRestClient: AzureGraphRestClient) : SaksbehandlerService(azureGraphRestClient) {

    override fun hentSaksbehandler(id: String): Saksbehandler = Saksbehandler(azureId = UUID.randomUUID(),
                                                                              navIdent = id,
                                                                              fornavn = "Mocka",
                                                                              etternavn = "Saksbehandler")
}
