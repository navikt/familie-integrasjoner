package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.integrasjoner.dokdist.api.DistribuerJournalpostRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dist")
class DokdistController(private val dokdistService: DokdistService) {

    @ExceptionHandler(KanIkkeDistribuereJournalpostException::class)
    fun handleKanIkkeDistribuereException(ex: KanIkkeDistribuereJournalpostException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Feil ved distribusjon {}", ex.message)
        return ResponseEntity.badRequest().body(Ressurs.failure(null, ex))
    }


    @PostMapping("v1")
    @ResponseStatus(HttpStatus.OK)
    fun distribuerJournalpost(@RequestBody request: @Valid DistribuerJournalpostRequest)
            : ResponseEntity<Ressurs<String>>  {

        val response = dokdistService.distribuerDokumentForJournalpost(request.journalpostId, request.dokumentProdApp)
        return ResponseEntity.ok(success(response?.bestillingsId ?: throw NullResponseException()))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokdistController::class.java)
    }

}