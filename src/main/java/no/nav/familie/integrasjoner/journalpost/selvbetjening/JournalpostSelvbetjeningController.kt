package no.nav.familie.integrasjoner.journalpost.selvbetjening

import kotlinx.coroutines.runBlocking
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import no.nav.familie.integrasjoner.safselvbetjening.generated.hentdokumentoversikt.Dokumentoversikt
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/journalpostselvbetjening")
@RequiredIssuers(
    ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"]),
)
class JournalpostSelvbetjeningController(
    private val safSelvbetjeningService: SafSelvbetjeningService,
) {
    @GetMapping("/dokumentoversikt/{tema}")
    fun hentDokumentoversikt(
        @PathVariable tema: Tema,
    ): ResponseEntity<Dokumentoversikt> = ResponseEntity.ok(runBlocking { safSelvbetjeningService.hentDokumentoversiktForIdent(EksternBrukerUtils.hentFnrFraToken(), tema = tema) })

    @GetMapping("/{journalpostId}/dokument/{dokumentInfoId}")
    fun hentDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
    ): ResponseEntity<ByteArray> = ResponseEntity.ok(safSelvbetjeningService.hentDokument(journalpostId, dokumentInfoId))
}
