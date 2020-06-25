package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.personopplysning.domene.Ident
import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.personopplysning.internal.IdentInformasjon
import no.nav.familie.integrasjoner.personopplysning.internal.Person
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

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["aktorId/{tema}"])
    fun aktørId(@RequestBody(required = true) ident: Ident,
                @PathVariable tema: Tema): ResponseEntity<Ressurs<List<String>>> {
        return ResponseEntity.ok().body(success(data = personopplysningerService.hentAktørId(ident.ident, tema.toString()),
                                                melding = "Hent aktørId OK"))
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["identer/{tema}"])
    fun identer(@RequestBody(required = true) ident: Ident,
                @PathVariable tema: Tema): ResponseEntity<Ressurs<List<IdentInformasjon>>> {
        return ResponseEntity.ok().body(success(data = personopplysningerService.hentIdenter(ident.ident, tema.toString(), false),
                                                melding = "Hent identer OK"))
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["identer/{tema}/historikk"])
    fun identerHistoriske(@RequestBody(required = true) ident: Ident,
                          @PathVariable tema: Tema): ResponseEntity<Ressurs<List<IdentInformasjon>>> {
        return ResponseEntity.ok().body(success(data = personopplysningerService.hentIdenter(ident.ident, tema.toString(), true),
                                                melding = "Hent historiske identer OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/historikk"])
    fun historikk(@RequestHeader(name = "Nav-Personident") personIdent: String,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fomDato: LocalDate,
                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) tomDato: LocalDate)
            : ResponseEntity<Ressurs<PersonhistorikkInfo>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentHistorikkFor(personIdent, fomDato, tomDato),
                                                "Hent personhistorikk OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/info"])
    fun personInfo(@RequestHeader(name = "Nav-Personident") personIdent: String): ResponseEntity<Ressurs<Personinfo>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentPersoninfoFor(personIdent),
                                                "Hent personinfo OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/info/{tema}"])
    fun personInfo(@RequestHeader(name = "Nav-Personident") personIdent: String,
                   @PathVariable tema: Tema): ResponseEntity<Ressurs<Person>> {
        return ResponseEntity.ok().body(success(
                personopplysningerService.hentPersoninfo(personIdent, tema.toString(), PersonInfoQuery.MED_RELASJONER),
                "Hent personinfo OK"))
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/infoEnkel/{tema}"])
    fun personInfoEnkel(@RequestHeader(name = "Nav-Personident") personIdent: String,
                        @PathVariable tema: Tema): ResponseEntity<Ressurs<Person>> {
        return ResponseEntity.ok().body(success(
                personopplysningerService.hentPersoninfo(personIdent, tema.toString(), PersonInfoQuery.ENKEL),
                "Hent personinfo OK"))
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["doedsfall/{tema}"])
    fun dødsfall(@RequestBody(required = true) ident: Ident,
                 @PathVariable tema: Tema): ResponseEntity<Ressurs<DødsfallResponse>> {
        return ResponseEntity.ok().body(success(personopplysningerService.hentDødsfall(ident.ident, tema.toString()),
                                                "Hent dødsfall OK"))
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["harVerge/{tema}"])
    fun harVergeEllerFullmektig(@RequestBody(required = true) ident: Ident,
                                @PathVariable tema: Tema): ResponseEntity<Ressurs<VergeResponse>> {
        return ResponseEntity.ok().body(success(personopplysningerService.harVergeEllerFullmektig(ident.ident, tema.toString()),
                                                "Hent vergeopplysninger OK"))
    }

    enum class Tema {
        KON,
        BAR,
        ENF
    }

}
