package no.nav.familie.integrasjoner.arbeidoginntekt

import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/arbeid-og-inntekt")
@ProtectedWithClaims(issuer = "azuread")
class ArbeidOgInntektController(
    private val arbeidOgInntektService: ArbeidOgInntektService,
) {
    @PostMapping("hent-url")
    fun hentUrl(
        @RequestBody(required = true) personIdentRequest: PersonIdent,
    ): Ressurs<String> = Ressurs.success(arbeidOgInntektService.hentArbeidOgInntektUrl(personIdentRequest))
}
