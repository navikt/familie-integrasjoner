package no.nav.familie.ef.mottak.api.kodeverk

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Unprotected
@RequestMapping(path = ["/api/kodeverk/poststed"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PostnummerController(val kodeverkService: KodeverkService) {

    @GetMapping("/{postnummer}")
    fun hentPoststedFor(@PathVariable postnummer: String): String {
        return kodeverkService.hentPoststedFor(postnummer)
    }
}
