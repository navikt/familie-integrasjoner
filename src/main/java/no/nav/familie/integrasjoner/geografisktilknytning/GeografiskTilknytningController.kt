package no.nav.familie.integrasjoner.geografisktilknytning

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/api/arbeidsfordeling")
@ProtectedWithClaims(issuer = "azuread")
class GeografiskTilknytningController(
        private val pdlRestClient: PdlRestClient
) {

    @PostMapping("/geografisk-tilknytning/{tema}")
    fun hentGeografiskTilknytning(@PathVariable(name = "tema") tema: Tema,
                                             @RequestBody personIdent: PersonIdent)
            : ResponseEntity<Ressurs<String>> {
        return ResponseEntity.ok(success(pdlRestClient.hentGeografiskTilknytning(personIdent.ident, tema.toString())))
    }

    enum class Tema {
        KON,
        BAR,
        ENF
    }

}