package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dist")
class DokdistController(private val dokdistService: DokdistService) {



    @GetMapping("v1/{journalpostId}")
    @ResponseStatus(HttpStatus.OK)
    fun distribuerJournalpost(@PathVariable(name = "journalpostId") journalpostId: String): ResponseEntity<Ressurs<String>>  {

        val resp = dokdistService.distribuerDokumentForJournalpost(journalpostId)
        return ResponseEntity.ok(success(resp?.bestillingsId ?: ""))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokdistController::class.java)
    }

}