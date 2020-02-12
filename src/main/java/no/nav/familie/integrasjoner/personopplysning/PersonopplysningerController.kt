package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsDato
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.ikkeTilgang
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpClientErrorException.Forbidden
import java.time.LocalDate
import javax.validation.constraints.NotNull

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/personopplysning")
class PersonopplysningerController(private val personopplysningerService: PersonopplysningerService) {

    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleRestClientResponseException(e: HttpClientErrorException.NotFound): ResponseEntity<Ressurs<Any>> {
        return ResponseEntity.status(e.rawStatusCode)
                .body(failure("Feil mot personopplysning. ${e.rawStatusCode} Message=${e.message}", null))
    }

    @ExceptionHandler(Forbidden::class)
    fun handleRestClientResponseException(e: Forbidden): ResponseEntity<Ressurs<Any>> {
        return ResponseEntity.status(e.rawStatusCode)
                .body(ikkeTilgang("Ikke tilgang mot personopplysning ${e.message}"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/historikk"])
    fun historikk(@RequestHeader(name = "Nav-Personident") @NotNull personIdent: String?,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull fomDato: LocalDate?,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull tomDato: LocalDate?)
            : ResponseEntity<Ressurs<PersonhistorikkInfo>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentHistorikkFor(personIdent, fomDato, tomDato),
                                                "Hent personhistorikk OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/info"])
    fun personInfo(@RequestHeader(name = "Nav-Personident") @NotNull personIdent: String?): ResponseEntity<Ressurs<Personinfo>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentPersoninfoFor(personIdent),
                                                "Hent personinfo OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/info/{tema}"])
    fun personInfo(@RequestHeader(name = "Nav-Personident") @NotNull personIdent: String,
                   @PathVariable tema: Tema): ResponseEntity<Ressurs<PdlFødselsDato>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentPersoninfo(personIdent, tema.toString()),
                                                "Hent personinfo OK"))
    }

    enum class Tema {
        KON,
        BAR,
        ENF
    }

}