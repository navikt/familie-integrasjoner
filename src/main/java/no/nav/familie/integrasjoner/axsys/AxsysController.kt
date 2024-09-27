package no.nav.familie.integrasjoner.axsys

import no.nav.familie.kontrakter.felles.enhet.Enhet
import no.nav.familie.kontrakter.felles.enhet.HentEnheterNavIdentHarTilgangTilRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/enhetstilganger")
class AxsysController(
    private val axsysService: AxsysService,
) {
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun hentEnheterNavIdentHarTilgangTil(
        @RequestBody(required = true) hentEnheterNavIdentHarTilgangTilRequest: HentEnheterNavIdentHarTilgangTilRequest,
    ): ResponseEntity<List<Enhet>> {
        val (navIdent, tema) = hentEnheterNavIdentHarTilgangTilRequest
        return ResponseEntity.ok(axsysService.hentEnheterNavIdentHarTilgangTil(navIdent, tema))
    }
}
