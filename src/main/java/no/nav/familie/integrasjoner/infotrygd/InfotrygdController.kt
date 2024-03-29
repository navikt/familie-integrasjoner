package no.nav.familie.integrasjoner.infotrygd

import no.nav.familie.integrasjoner.client.rest.InfotrygdRestClient
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/infotrygd")
class InfotrygdController(
    private val infotrygdRestClient: InfotrygdRestClient,
) {
    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleExceptions(ex: HttpStatusCodeException): ResponseEntity<Ressurs<Any>> {
        if (ex is HttpClientErrorException.NotFound) {
            LOG.info("404 mot infotrygd-kontantstotte")
        } else {
            LOG.error("Oppslag mot infotrygd-kontantstotte feilet. Status code: {}", ex.statusCode)
            secureLogger.error(
                "Oppslag mot infotrygd-kontantstotte feilet. feilmelding={} responsebody={} exception={}",
                ex.message,
                ex.responseBodyAsString,
                ex,
            )
        }
        return ResponseEntity
            .status(ex.statusCode)
            .body(failure("Oppslag mot infotrygd-kontanstøtte feilet ${ex.responseBodyAsString}", error = ex))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/harBarnAktivKontantstotte"])
    fun aktivKontantstøtte(
        @RequestHeader(name = "Nav-Personident")
        fnr: String,
    ): ResponseEntity<Ressurs<AktivKontantstøtteInfo>> {
        if (!fnr.matches(Regex("[0-9]+"))) {
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "fnr må være et tall")
        }
        return ResponseEntity.ok(
            success(
                infotrygdRestClient.hentAktivKontantstøtteFor(fnr),
                "Oppslag mot Infotrygd OK",
            ),
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(InfotrygdController::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
