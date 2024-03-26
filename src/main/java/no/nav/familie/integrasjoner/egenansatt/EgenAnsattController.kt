package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.egenansatt.EgenAnsattResponse
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/egenansatt")
class EgenAnsattController(private val egenAnsattService: EgenAnsattService) {
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun erEgenAnsatt(
        @RequestBody(required = true) ident: Ident,
    ): ResponseEntity<Ressurs<EgenAnsattResponse>> {
        return ResponseEntity.ok().body(success(data = EgenAnsattResponse(egenAnsattService.erEgenAnsatt(ident.ident))))
    }
}
