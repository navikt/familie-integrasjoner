package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/saksbehandler"])
@Profile("!e2e")
class SaksbehandlerController(private val saksbehandlerService: SaksbehandlerService) {

    @GetMapping(path = ["/{id}"])
    @ProtectedWithClaims(issuer = "azuread")
    fun hentSaksbehandler(@PathVariable id: String): Saksbehandler { // id kan være azure-id eller e-post
        return saksbehandlerService.hentSaksbehandler(id)
    }

}
