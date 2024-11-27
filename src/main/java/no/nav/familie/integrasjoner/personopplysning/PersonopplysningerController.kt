package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollUtil
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.ikkeTilgang
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.Ident
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpClientErrorException.Forbidden

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/personopplysning")
class PersonopplysningerController(
    private val personopplysningerService: PersonopplysningerService,
) {
    @ExceptionHandler(HttpClientErrorException.NotFound::class)
    fun handleRestClientResponseException(e: HttpClientErrorException.NotFound): ResponseEntity<Ressurs<Any>> =
        ResponseEntity
            .status(e.statusCode.value())
            .body(failure("Feil mot personopplysning. ${e.statusCode.value()} Message=${e.message}", null))

    @ExceptionHandler(Forbidden::class)
    fun handleRestClientResponseException(e: Forbidden): ResponseEntity<Ressurs<Any>> =
        ResponseEntity
            .status(e.statusCode.value())
            .body(ikkeTilgang("Ikke tilgang mot personopplysning ${e.message}"))

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["v1/identer/{tema}"])
    fun hentIdenter(
        @RequestBody(required = true) ident: Ident,
        @PathVariable tema: Tema,
        @RequestParam(
            value = "historikk",
            required = false,
            defaultValue = "false",
        ) medHistorikk: Boolean,
    ): Ressurs<FinnPersonidenterResponse> = success(personopplysningerService.hentIdenter(ident.ident, tema, medHistorikk))

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE], path = ["strengeste-adressebeskyttelse-for-person-med-relasjoner"])
    fun hentStrengesteAdressebeskyttelseForPersonMedRelasjoner(
        @RequestBody personIdent: PersonIdent,
        @RequestHeader(name = "Nav-Tema")
        tema: Tema,
    ): Ressurs<ADRESSEBESKYTTELSEGRADERING> {
        val personMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner(personIdent.ident, tema)
        return success(TilgangskontrollUtil.høyesteGraderingen(personMedRelasjoner) ?: ADRESSEBESKYTTELSEGRADERING.UGRADERT)
    }
}
