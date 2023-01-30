package no.nav.familie.integrasjoner.sak

import no.nav.familie.integrasjoner.client.rest.SkyggesakRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/skyggesak")
class SkyggesakController(private val skyggesakRestClient: SkyggesakRestClient) {

    @PostMapping(path = ["/v1"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun opprettSkyggesak(@RequestBody request: Skyggesak): ResponseEntity<Ressurs<Unit>> {
        return ResponseEntity.ok(
            success(
                skyggesakRestClient.opprettSak(request),
                "Oppretter skyggesak for fagsak ${request.fagsakNr}",
            ),
        )
    }
}
