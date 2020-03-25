package no.nav.familie.integrasjoner.aktør

import no.nav.familie.integrasjoner.personopplysning.domene.AktørId
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/aktoer")
class AktørController(private val aktørService: AktørService) {

    @GetMapping("v1")
    fun getAktørIdForPersonIdent(@RequestHeader(name = "Nav-Personident")
                                 personIdent: @NotNull String?): ResponseEntity<Ressurs<Map<String, String>>> {
        return ResponseEntity.ok()
                .body(success(mapOf("aktørId" to aktørService.getAktørId(personIdent)),
                              "Hent aktør for personident OK"))
    }

    @GetMapping(path = ["v1/fraaktorid"])
    fun getPersonIdentForAktørId(@RequestHeader(name = "Nav-Aktorid")
                                 aktørId: @NotNull String?): ResponseEntity<Ressurs<Map<String, String>>> {
        return ResponseEntity.ok()
                .body(success(mapOf("personIdent" to aktørService.getPersonIdent(AktørId(aktørId))),
                              "Hent personIdent for aktør OK")
                )
    }

}
