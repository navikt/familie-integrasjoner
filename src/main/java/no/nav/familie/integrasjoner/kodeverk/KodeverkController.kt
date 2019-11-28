package no.nav.familie.integrasjoner.kodeverk

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@Unprotected
@RequestMapping(path = ["/api/kodeverk/poststed"], produces = [MediaType.TEXT_PLAIN_VALUE])
class PostnummerController(val kodeverkService: KodeverkService) {

    @GetMapping("/{postnummer}")
    fun hentPoststedFor(@PathVariable postnummer: String): String {
        return kodeverkService.hentPoststedFor(postnummer)
    }
}
