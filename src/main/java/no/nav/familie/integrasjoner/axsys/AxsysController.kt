package no.nav.familie.integrasjoner.axsys

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/axsys")
class AxsysController(
    private val axsysService: AxsysService,
) {
    // TODO : Finn ut om vi kan bruke GET, hvis ikke dokumenter hvorfor vi bruker POST
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun hentTilgang(
        @RequestBody(required = true) saksbehandlerId: SaksbehandlerId,
    ): TilgangV2DTO = axsysService.hentTilgang(saksbehandlerId)
}
