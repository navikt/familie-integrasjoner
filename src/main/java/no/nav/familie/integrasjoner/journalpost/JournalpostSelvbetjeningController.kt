package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/journalpostselvbetjening")
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"]),
)
class JournalpostSelvbetjeningController {
    @GetMapping("/hello")
    fun hello(): ResponseEntity<String> = ResponseEntity.ok("Hello")

//    TODO: Implementer
//    @GetMapping()
//    fun hentJournalposter(): Ressurs<List<Journalpost>> = journalpostService.finnJournalposter()
}
