package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.dokarkiv.api.ArkiverDokumentRequest
import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.ks.kontrakter.sak.Ressurs
import no.nav.familie.ks.kontrakter.sak.Ressurs.Companion.failure
import no.nav.familie.ks.kontrakter.sak.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.util.function.Consumer
import javax.validation.Valid

@RestController @ProtectedWithClaims(issuer = "azuread") @RequestMapping("/api/arkiv")
class DokarkivController(private val journalføringService: DokarkivService) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Ressurs> {
        val errors: MutableMap<String, String> = HashMap()
        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage() ?: ""
            errors[fieldName] = errorMessage
        })
        LOG.warn("Valideringsfeil av input ved arkivering: $errors")
        return ResponseEntity.badRequest().body(failure("Valideringsfeil av input ved arkivering $errors", ex))
    }

    @ExceptionHandler(KotlinNullPointerException::class)
    fun handleKotlinNull(ex: KotlinNullPointerException): ResponseEntity<Ressurs> {
        LOG.warn("Nullpointer på input ved arkivering: ${ex.message}")
        return ResponseEntity.badRequest().body(failure("Valideringsfeil av input ved arkivering ${ex.message}", ex))
    }

    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException::class)
    fun handleKanIkkeFerdigstilleException(ex: KanIkkeFerdigstilleJournalpostException): ResponseEntity<Ressurs> {
        LOG.warn("Feil ved ferdigstilling {}", ex.message)
        return ResponseEntity.badRequest().body(failure(null, ex))
    }

    @PostMapping(path = ["v1"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokument(@RequestBody arkiverDokumentRequest: @Valid ArkiverDokumentRequest): ResponseEntity<Ressurs> {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(journalføringService.lagInngåendeJournalpost(arkiverDokumentRequest),
                              "Arkivert journalpost OK"))
    }

    @PutMapping("v1/{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    fun ferdigstillJournalpost(@PathVariable(name = "journalpostId") journalpostId: String?,
                               @RequestParam(name = "journalfoerendeEnhet")
                               journalførendeEnhet: String?): ResponseEntity<Ressurs> {
        if (journalpostId == null) {
            return ResponseEntity.badRequest().body(failure("journalpostId er null", null))
        }
        journalføringService.ferdistillJournalpost(journalpostId, journalførendeEnhet)
        return ResponseEntity.ok(success(mapOf("journalpostId" to journalpostId),
                                         "Ferdigstilt journalpost $journalpostId"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokarkivController::class.java)
    }

}