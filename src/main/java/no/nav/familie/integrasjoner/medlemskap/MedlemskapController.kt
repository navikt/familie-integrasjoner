package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsInfo
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("api/medlemskap")
class MedlemskapController(private val medlemskapService: MedlemskapService) {

    @GetMapping("v1")
    fun hentMedlemskapsunntak(@RequestParam("id") aktørId: String?): ResponseEntity<Ressurs<MedlemskapsInfo>> {
        return ResponseEntity.ok(success(medlemskapService.hentMedlemskapsUnntak(aktørId),
                                         "Henting av medlemskapsunntak OK"))
    }

}