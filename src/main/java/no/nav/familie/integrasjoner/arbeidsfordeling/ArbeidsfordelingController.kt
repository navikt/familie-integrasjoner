package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/arbeidsfordeling")
@ProtectedWithClaims(issuer = "azuread")
class ArbeidsfordelingController(
    private val service: ArbeidsfordelingService,
) {
    @PostMapping("/enhet/{tema}")
    fun hentBehandlendeEnhetForPersonIdentV2(
        @PathVariable(name = "tema") tema: Tema,
        @RequestBody personIdent: PersonIdent,
    ): ResponseEntity<Ressurs<List<Enhet>>> = ResponseEntity.ok(success(service.finnBehandlendeEnhetForPerson(personIdent.ident, tema)))

    @PostMapping("/oppfolging/{tema}")
    fun hentOppfølgingsenhetForPerson(
        @PathVariable(name = "tema") tema: Tema,
        @RequestBody personIdent: PersonIdent,
    ): ResponseEntity<Ressurs<List<Enhet>>> = ResponseEntity.ok(success(service.finnBehandlendeEnhetForOppfølging(personIdent.ident, tema)))

    @PostMapping("/enhet/{tema}/med-relasjoner")
    fun hentBehandlendeEnhetForPersonIdentMedRelasjoner(
        @PathVariable(name = "tema") tema: Tema,
        @RequestBody
        personIdent: PersonIdent,
    ): ResponseEntity<Ressurs<List<Enhet>>> = ResponseEntity.ok(success(service.finnBehandlendeEnhetForPersonMedRelasjoner(personIdent.ident, tema)))

    @PostMapping("/nav-kontor/{tema}")
    fun hentLokaltNavKontor(
        @PathVariable(name = "tema") tema: Tema,
        @RequestBody personIdent: PersonIdent,
    ): ResponseEntity<Ressurs<NavKontorEnhet?>> = ResponseEntity.ok(success(service.finnLokaltNavKontor(personIdent.ident, tema)))

    @GetMapping("/nav-kontor/{enhetsid}")
    fun hentNavKontor(
        @PathVariable(name = "enhetsid") enhetsId: String,
    ): ResponseEntity<Ressurs<NavKontorEnhet>> = ResponseEntity.ok(success(service.hentNavKontor(enhetsId)))
}
