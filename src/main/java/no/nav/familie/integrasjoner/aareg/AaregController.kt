package no.nav.familie.integrasjoner.aareg

import no.nav.familie.integrasjoner.aareg.domene.Arbeidsforhold
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/aareg")
@ProtectedWithClaims(issuer = "azuread")
class AaregController(val aaregService: AaregService) {

    @PostMapping(path = ["/arbeidsforhold"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentArbeidsforhold(@RequestBody(required = true) arbeidsforholdRequest: ArbeidsforholdRequest): Ressurs<List<Arbeidsforhold>> {
        val arbeidsforhold = aaregService.hentArbeidsforhold(arbeidsforholdRequest.personIdent, arbeidsforholdRequest.ansettelsesperiodeFom)
        return success(arbeidsforhold)
    }
}

class ArbeidsforholdRequest(
    val personIdent: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val ansettelsesperiodeFom: LocalDate
)
