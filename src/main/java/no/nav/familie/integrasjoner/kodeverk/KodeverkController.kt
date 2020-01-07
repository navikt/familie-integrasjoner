package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Unprotected
@RequestMapping(path = ["/api/kodeverk"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KodeverkController(val kodeverkService: KodeverkService) {

    @GetMapping("/poststed/{postnummer}")
    fun hentPoststedFor(@PathVariable postnummer: String): ResponseEntity<Ressurs<String>> {
        return ResponseEntity.ok(Ressurs.Companion.success(kodeverkService.hentPoststedFor(postnummer)))
    }
}
