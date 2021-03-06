package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokarkiv.*
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.function.Consumer
import javax.validation.Valid
import no.nav.familie.kontrakter.felles.arkivering.ArkiverDokumentRequest as DeprecatedArkiverDokumentRequest

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/arkiv")
class DokarkivController(private val journalføringService: DokarkivService) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Ressurs<Any>> {
        val errors: MutableMap<String, String> = HashMap()
        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage() ?: ""
            errors[fieldName] = errorMessage
        })
        LOG.warn("Valideringsfeil av input ved arkivering: $errors")
        return ResponseEntity.badRequest().body(failure("Valideringsfeil av input ved arkivering $errors", error = ex))
    }

    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException::class)
    fun handleKanIkkeFerdigstilleException(ex: KanIkkeFerdigstilleJournalpostException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Feil ved ferdigstilling {}", ex.message)
        return ResponseEntity.badRequest().body(failure(ex.message, error = ex))
    }

    @PostMapping(path = ["/v2"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokumentV2(@RequestBody @Valid deprecatedArkiverDokumentRequest: DeprecatedArkiverDokumentRequest)
            : ResponseEntity<Ressurs<ArkiverDokumentResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(journalføringService.lagJournalpostV2(deprecatedArkiverDokumentRequest),
                              "Arkivert journalpost OK"))
    }

    @PostMapping(path = ["/v3"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokumentV3(@RequestBody @Valid arkiverDokumentRequest: ArkiverDokumentRequest)
            : ResponseEntity<Ressurs<ArkiverDokumentResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(journalføringService.lagJournalpostV3(arkiverDokumentRequest),
                              "Arkivert journalpost OK"))
    }

    @PutMapping(path = ["/v2/{journalpostId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun oppdaterJournalpost(@PathVariable(name = "journalpostId") journalpostId: String,
                            @RequestBody @Valid oppdaterJournalpostRequest: OppdaterJournalpostRequest)
            : ResponseEntity<Ressurs<OppdaterJournalpostResponse>> {
        val response = journalføringService.oppdaterJournalpost(oppdaterJournalpostRequest, journalpostId)
        return ResponseEntity.ok(success(response, "Oppdatert journalpost $journalpostId sakstilknyttning"))
    }

    @PutMapping("/v2/{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    fun ferdigstillJournalpost(@PathVariable(name = "journalpostId") journalpostId: String,
                               @RequestParam(name = "journalfoerendeEnhet")
                               journalførendeEnhet: String): ResponseEntity<Ressurs<Map<String, String>>> {

        journalføringService.ferdistillJournalpost(journalpostId, journalførendeEnhet)
        return ResponseEntity.ok(success(mapOf("journalpostId" to journalpostId),
                                         "Ferdigstilt journalpost $journalpostId"))
    }


    @PostMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun leggTilLogiskVedlegg(@PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
                             @RequestBody @Valid request: LogiskVedleggRequest)
            : ResponseEntity<Ressurs<LogiskVedleggResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(journalføringService.lagNyttLogiskVedlegg(dokumentinfoId, request),
                              "Opprettet logisk vedlegg"))
    }

    @DeleteMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg/{logiskVedleggId}"])
    fun slettLogiskVedlegg(@PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
                           @PathVariable(name = "logiskVedleggId") logiskVedleggId: String)
            : ResponseEntity<Ressurs<LogiskVedleggResponse>> {

        journalføringService.slettLogiskVedlegg(dokumentinfoId, logiskVedleggId)
        return ResponseEntity.status(HttpStatus.OK)
                .body(success(LogiskVedleggResponse(logiskVedleggId.toLong()), "logisk vedlegg slettet"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokarkivController::class.java)
    }

}