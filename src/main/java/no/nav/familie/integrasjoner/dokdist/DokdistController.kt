package no.nav.familie.integrasjoner.dokdist

import jakarta.validation.Valid
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokdist.DistribuerJournalpostRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dist")
class DokdistController(
    private val dokdistService: DokdistService,
) {
    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientException(e: HttpClientErrorException): ResponseEntity<Ressurs<Any>> {
        secureLogger.warn("Feil ved distribusjon: ${e.message}")
        val ressurs: Ressurs<Any> =
            Ressurs(
                data = e.responseBodyAsString,
                status = Ressurs.Status.FEILET,
                melding = e.message ?: "Uventet feil status=${e.statusCode.value()}",
                stacktrace = e.stackTraceToString(),
            )
        return ResponseEntity.status(e.statusCode.value()).body(ressurs)
    }

    @PostMapping("v1")
    @ResponseStatus(HttpStatus.OK)
    fun distribuerJournalpost(
        @RequestBody request: @Valid DistribuerJournalpostRequest,
    ): ResponseEntity<Ressurs<String>> {
        val response = dokdistService.distribuerDokumentForJournalpost(request)
        return ResponseEntity.ok(success(response?.bestillingsId ?: throw NullResponseException()))
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
