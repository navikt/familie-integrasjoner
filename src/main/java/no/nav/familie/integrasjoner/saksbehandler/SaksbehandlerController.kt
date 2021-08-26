package no.nav.familie.integrasjoner.saksbehandler

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/saksbehandler"])
class SaksbehandlerController(private val saksbehandlerService: SaksbehandlerService) {

    @GetMapping(path = ["/{id}"])
    @ProtectedWithClaims(issuer = "azuread")
    fun hentSaksbehandler(@PathVariable id: String): Ressurs<Saksbehandler> { // id kan være azure-id, e-post eller nav-ident
        return Ressurs.success(saksbehandlerService.hentSaksbehandler(id), "Hent saksbehandler OK")
    }

}
