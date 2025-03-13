package no.nav.familie.integrasjoner.modiacontextholder

import no.nav.familie.integrasjoner.client.rest.ModiaContextHolderClient
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderNyAktivBrukerDto
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/modia-context-holder")
class ModiaContextHolderController(
    private val modiaContextHolderClient: ModiaContextHolderClient,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentContext(): Ressurs<ModiaContextHolderResponse> = success(modiaContextHolderClient.hentContext())

    @PostMapping(
        path = ["/sett-aktiv-bruker"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun settNyAktivBruker(
        @RequestBody nyAktivBrukerDto: ModiaContextHolderNyAktivBrukerDto,
    ): Ressurs<ModiaContextHolderResponse> = success(modiaContextHolderClient.settContext(nyAktivBrukerDto.toRequest()))
}
