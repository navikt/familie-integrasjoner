package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping(path = ["/api/selvbetjening/kodeverk", "/api/kodeverk"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KodeverkController(val kodeverkService: KodeverkService) {

    @GetMapping("/poststed")
    fun hentPoststed(): ResponseEntity<Ressurs<KodeverkDto>> {
        return ResponseEntity.ok(Ressurs.Companion.success(kodeverkService.hentPostnummerMedHistorikk()))
    }

    @GetMapping("/poststed/{postnummer}")
    fun hentPoststed(@PathVariable postnummer: String): ResponseEntity<Ressurs<String>> {
        return ResponseEntity.ok(Ressurs.Companion.success(kodeverkService.hentPoststed(postnummer)))
    }

    @GetMapping("/landkoder")
    fun hentLandkoder(): ResponseEntity<Ressurs<KodeverkDto>> {
        return ResponseEntity.ok(Ressurs.Companion.success(kodeverkService.hentLandkoderMedHistorikk()))
    }

    @GetMapping("/landkoder/{landkode}")
    fun hentLandkod(@PathVariable landkode: String): ResponseEntity<Ressurs<String>> {
        return ResponseEntity.ok(Ressurs.Companion.success(kodeverkService.hentLandkode(landkode)))
    }
}
