package no.nav.familie.integrasjoner.dokdist

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import javax.validation.Valid

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dist")
class DokdistController(private val dokdistService: DokdistService) {

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientException(e: HttpClientErrorException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Feil ved distribusjon: ${e.message}")
        return ResponseEntity.status(e.rawStatusCode).body(Ressurs.failure(e.message, error = e))
    }


    @PostMapping("v1")
    @ResponseStatus(HttpStatus.OK)
    fun distribuerJournalpost(@RequestBody request: @Valid DistribuerJournalpostRequest)
            : ResponseEntity<Ressurs<String>>  {

        val response = dokdistService.distribuerDokumentForJournalpost(request)
        return ResponseEntity.ok(success(response?.bestillingsId ?: throw NullResponseException()))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokdistController::class.java)
    }

}