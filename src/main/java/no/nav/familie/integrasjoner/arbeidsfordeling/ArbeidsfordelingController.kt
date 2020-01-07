package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotNull

@RestController
@RequestMapping("/api/arbeidsfordeling")
@ProtectedWithClaims(issuer = "azuread")
class ArbeidsfordelingController(
        private val service: ArbeidsfordelingService
) {

    @GetMapping("/enhet/")
    fun hentBehandlendeEnhet(@RequestParam(name = "tema") tema: Tema,
                             @RequestParam(name = "geografi", required = false) geografi: String?,
                             @RequestParam(name = "diskresjonskode", required = false) diskresjonskode: String?)
            : ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> {
        return ResponseEntity.ok(success(service.finnBehandlendeEnhet(tema.toString(), geografi, diskresjonskode)))
    }

    @GetMapping("/enhet/{tema}")
    fun hentBehandlendeEnhetForPersonIdent(@NotNull @RequestHeader(name = "Nav-Personident") personIdent: String,
                                           @PathVariable(name = "tema") tema: Tema)
            : ResponseEntity<Ressurs<List<ArbeidsfordelingClient.Enhet>>> {
        return ResponseEntity.ok(success(service.finnBehandlendeEnhetForPerson(personIdent, tema.toString())))
    }

    enum class Tema {
        KON,
        BAR,
        ENF
    }

}