package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/journalpost")
@ProtectedWithClaims(issuer = "azuread")
class HentJournalpostController(
    private val journalpostService: JournalpostService,
) {
    @GetMapping
    fun hentJournalpost(
        @RequestParam(name = "journalpostId") journalpostId: String,
    ): ResponseEntity<Ressurs<Journalpost>> = ResponseEntity.ok(success(journalpostService.hentJournalpost(journalpostId), "OK"))

    @GetMapping("tilgangsstyrt/baks")
    fun hentTilgangsstyrtBaksJournalpost(
        @RequestParam(name = "journalpostId") journalpostId: String,
    ): ResponseEntity<Ressurs<Journalpost>> = ResponseEntity.ok(success(journalpostService.hentTilgangsstyrtBaksJournalpost(journalpostId), "OK"))

    @PostMapping
    fun hentJournalpostForBruker(
        @RequestBody journalposterForBrukerRequest: JournalposterForBrukerRequest,
    ): ResponseEntity<Ressurs<List<Journalpost>>> = ResponseEntity.ok(success(journalpostService.finnJournalposter(journalposterForBrukerRequest), "OK"))

    @PostMapping("tilgangsstyrt/baks")
    fun hentTilgangsstyrteJournalposterForBruker(
        @RequestBody journalposterForBrukerRequest: JournalposterForBrukerRequest,
    ): ResponseEntity<Ressurs<List<TilgangsstyrtJournalpost>>> = ResponseEntity.ok(success(journalpostService.finnTilgangsstyrteBaksJournalposter(journalposterForBrukerRequest), "OK"))

    @PostMapping("temaer")
    fun hentJournalpostForBrukerOgTema(
        @RequestBody journalposterForVedleggRequest: JournalposterForVedleggRequest,
    ): ResponseEntity<Ressurs<List<Journalpost>>> = ResponseEntity.ok(success(journalpostService.finnJournalposter(journalposterForVedleggRequest), "OK"))

    @GetMapping("hentdokument/{journalpostId}/{dokumentInfoId}")
    fun hentDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
        @RequestParam("variantFormat", required = false) variantFormat: String?,
    ): ResponseEntity<Ressurs<ByteArray>> =
        ResponseEntity.ok(
            success(
                journalpostService.hentDokument(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV"),
                "OK",
            ),
        )

    @GetMapping("hentdokument/tilgangsstyrt/baks/{journalpostId}/{dokumentInfoId}")
    fun hentTilgangsstyrtBaksDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
        @RequestParam("variantFormat", required = false) variantFormat: String?,
    ): ResponseEntity<Ressurs<ByteArray>> =
        ResponseEntity.ok(
            success(
                journalpostService.hentTilgangsstyrtBaksDokument(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV"),
                "OK",
            ),
        )
}
