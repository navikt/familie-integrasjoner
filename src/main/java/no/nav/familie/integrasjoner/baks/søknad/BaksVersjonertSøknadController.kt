package no.nav.familie.integrasjoner.baks.søknad

import no.nav.familie.kontrakter.ba.søknad.VersjonertBarnetrygdSøknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.ks.søknad.VersjonertKontantstøtteSøknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * VersjonertSøknad: søknadsdokument i en journalpost med variant "Original" (JSON) man kan bruke for automatisk prosessering.
 * Tar imot journalpost ID.
 */
@RestController
@RequestMapping("/api/baks/soknad")
@ProtectedWithClaims(issuer = "azuread")
class BaksVersjonertSøknadController(
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService,
) {
    @GetMapping("ba/{journalpostId}")
    fun hentBarnetrygdSøknadFraJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
    ): ResponseEntity<Ressurs<VersjonertBarnetrygdSøknad>> = ResponseEntity.ok(success(baksVersjonertSøknadService.hentVersjonertBarnetrygdSøknad(journalpostId), "OK"))

    @GetMapping("ks/{journalpostId}")
    fun hentKontantstøtteSøknadFraJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
    ): ResponseEntity<Ressurs<VersjonertKontantstøtteSøknad>> = ResponseEntity.ok(success(baksVersjonertSøknadService.hentVersjonertKontantstøtteSøknad(journalpostId), "OK"))
}
