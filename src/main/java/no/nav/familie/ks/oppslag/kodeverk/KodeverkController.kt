package no.nav.familie.ks.oppslag.kodeverk

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping(path = ["/api/kodeverk/poststed"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PostnummerController(val kodeverkService: KodeverkService) {

    @GetMapping("/{postnummer}")
    fun hentPoststedFor(@PathVariable postnummer: String): String {
        return kodeverkService.hentPoststedFor(postnummer)
    }
}
