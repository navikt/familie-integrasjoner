package no.nav.familie.integrasjoner.dokarkiv

import jakarta.validation.Valid
import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.BulkOppdaterLogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggRequest
import no.nav.familie.kontrakter.felles.dokarkiv.LogiskVedleggResponse
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.function.Consumer

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/arkiv")
class DokarkivController(
    private val journalføringService: DokarkivService,
) {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Ressurs<Any>> {
        val errors: MutableMap<String, String> = HashMap()
        ex.bindingResult.allErrors.forEach(
            Consumer { error: ObjectError ->
                val fieldName = (error as FieldError).field
                val errorMessage = error.getDefaultMessage() ?: ""
                errors[fieldName] = errorMessage
            },
        )
        LOG.warn("Valideringsfeil av input ved arkivering: $errors")
        return ResponseEntity.badRequest().body(failure("Valideringsfeil av input ved arkivering $errors", error = ex))
    }

    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException::class)
    fun handleKanIkkeFerdigstilleException(ex: KanIkkeFerdigstilleJournalpostException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Feil ved ferdigstilling {}", ex.message)
        return ResponseEntity.badRequest().body(failure(ex.message, error = ex))
    }

    @PostMapping(path = ["/v4"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokumentV4(
        @RequestBody @Valid
        arkiverDokumentRequest: ArkiverDokumentRequest,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
    ): ResponseEntity<Ressurs<ArkiverDokumentResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                success(
                    journalføringService.lagJournalpost(arkiverDokumentRequest, navIdent),
                    Companion.ARKIVERT_OK_MELDING,
                ),
            )

    @PutMapping(path = ["/v2/{journalpostId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun oppdaterJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
        @RequestBody @Valid
        oppdaterJournalpostRequest: OppdaterJournalpostRequest,
    ): ResponseEntity<Ressurs<OppdaterJournalpostResponse>> {
        val response = journalføringService.oppdaterJournalpost(oppdaterJournalpostRequest, journalpostId, navIdent)
        return ResponseEntity.ok(success(response, "Oppdatert journalpost $journalpostId sakstilknyttning"))
    }

    @PutMapping("/v2/{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    fun ferdigstillJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
        @RequestParam(name = "journalfoerendeEnhet") journalførendeEnhet: String,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
    ): ResponseEntity<Ressurs<Map<String, String>>> {
        journalføringService.ferdistillJournalpost(journalpostId, journalførendeEnhet, navIdent)
        return ResponseEntity.ok(
            success(
                mapOf("journalpostId" to journalpostId),
                "Ferdigstilt journalpost $journalpostId",
            ),
        )
    }

    @PostMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun leggTilLogiskVedlegg(
        @PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
        @RequestBody @Valid
        request: LogiskVedleggRequest,
    ): ResponseEntity<Ressurs<LogiskVedleggResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                success(
                    journalføringService.lagNyttLogiskVedlegg(dokumentinfoId, request),
                    "Opprettet logisk vedlegg",
                ),
            )

    @DeleteMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg/{logiskVedleggId}"])
    fun slettLogiskVedlegg(
        @PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
        @PathVariable(name = "logiskVedleggId") logiskVedleggId: String,
    ): ResponseEntity<Ressurs<LogiskVedleggResponse>> {
        journalføringService.slettLogiskVedlegg(dokumentinfoId, logiskVedleggId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(success(LogiskVedleggResponse(logiskVedleggId.toLong()), "logisk vedlegg slettet"))
    }

    @PutMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun oppdaterAlleLogiskeVedleggForDokument(
        @PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
        @RequestBody @Valid
        request: BulkOppdaterLogiskVedleggRequest,
    ): ResponseEntity<Ressurs<String>> {
        journalføringService.oppdaterLogiskeVedleggForDokument(dokumentinfoId, request)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(success(dokumentinfoId, "logiske vedlegg oppdatert for $dokumentinfoId"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DokarkivController::class.java)
        const val ARKIVERT_OK_MELDING = "Arkivert journalpost OK"
        const val NAV_USER_ID = "Nav-User-Id"
    }
}
