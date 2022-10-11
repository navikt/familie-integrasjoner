package no.nav.familie.integrasjoner.aktør

import no.nav.familie.integrasjoner.personopplysning.domene.AktørId
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/aktoer")
class AktørController(private val aktørService: AktørService) {

    @PostMapping("v2/{tema}")
    fun finnAktørIdForPersonIdent(
        @RequestBody(required = true) ident: Ident,
        @PathVariable tema: Tema
    ): ResponseEntity<Ressurs<Map<String, String>>> {

        return ResponseEntity.ok(
            success(
                mapOf("aktørId" to aktørService.getAktørIdFraPdl(ident.ident, tema)),
                "Hent aktør for personident OK"
            )
        )
    }

    @PostMapping("v2/fraaktorid/{tema}")
    fun finnPersonIdentForAktørId(
        @RequestBody(required = true) aktørId: String,
        @PathVariable tema: Tema
    ): ResponseEntity<Ressurs<Map<String, String>>> {

        return ResponseEntity.ok(
            success(
                mapOf("personIdent" to aktørService.getPersonIdentFraPdl(AktørId(aktørId), tema)),
                "Hent aktør for personident OK"
            )
        )
    }
}
