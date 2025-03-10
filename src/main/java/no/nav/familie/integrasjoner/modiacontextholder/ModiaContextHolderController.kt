package no.nav.familie.integrasjoner.modiacontextholder

import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderNyAktivBrukerDto
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/modia-context-holder")
class ModiaContextHolderController(
    private val modiaContextHolderService: ModiaContextHolderService,
) {
    @GetMapping
    fun hentContext(): ResponseEntity<Ressurs<ModiaContextHolderResponse>> =
        ResponseEntity.ok(
            success(modiaContextHolderService.hentContext()),
        )

    @PostMapping("/sett-aktiv-bruker")
    fun settNyAktivBruker(nyAktivBrukerDto: ModiaContextHolderNyAktivBrukerDto): ResponseEntity<Ressurs<ModiaContextHolderResponse>> =
        ResponseEntity.ok(
            success(modiaContextHolderService.settContext(nyAktivBrukerDto.toRequest())),
        )
}
