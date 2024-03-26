package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/saksbehandler"])
@Profile("e2e")
class SaksbehandlerControllerE2E {
    @GetMapping(path = ["/{id}"])
    @ProtectedWithClaims(issuer = "azuread")
    fun hentSaksbehandler(
        @PathVariable id: String,
    ): Ressurs<Saksbehandler> {
        return Ressurs.success(
            Saksbehandler(
                azureId = UUID.randomUUID(),
                navIdent = id,
                fornavn = "Mocka",
                etternavn = "Saksbehandler",
                enhet = "4408",
            ),
            "Hent saksbehandler OK",
        )
    }
}
