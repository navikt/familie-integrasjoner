package no.nav.familie.integrasjoner.infotrygdsak

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.infotrygd.sb.opprettsak.OpprettSakResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/infotrygdsak", produces = [MediaType.APPLICATION_JSON_VALUE])
class InfotrygdsakController(private val infotrygdService: InfotrygdService) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")


    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleExceptions(ex: HttpStatusCodeException): ResponseEntity<Ressurs<Any>> {
        logger.error("Feil ved opprettelse av infotrygdsak. Status code: {}", ex.statusCode)
        secureLogger.error("Feil ved opprettelse av infotrygdsak. feilmelding={} responsebody={} exception={}",
                           ex.message,
                           ex.responseBodyAsString,
                           ex)
        return ResponseEntity
                .status(ex.statusCode)
                .body(failure("Oppslag mot infotrygd-kontanstøtte feilet ${ex.responseBodyAsString}", error = ex))
    }

    @PostMapping(path = ["gosys"])
    fun oppretttInfotrygdsakForGosys(@RequestBody opprettInfotrygdSakRequest: OpprettInfotrygdSakRequest)
            : Ressurs<OpprettInfotrygdSakResponse> {
        return success(infotrygdService.opprettInfotrygdsakGosys(opprettInfotrygdSakRequest))
    }

    @PostMapping(path = ["infotrygd"])
    fun oppretttInfotrygdsakMedOpprettsak(@RequestBody opprettSakRequest: OpprettSakRequest)
            : Ressurs<OpprettSakResponse> {
        return success(infotrygdService.opprettInfotrygdsak(opprettSakRequest))
    }

}