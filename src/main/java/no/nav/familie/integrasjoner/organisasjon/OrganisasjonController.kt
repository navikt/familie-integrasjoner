package no.nav.familie.integrasjoner.organisasjon

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/organisasjon")
class OrganisasjonController(private val organisasjonService: OrganisasjonService) {

    @GetMapping(path = ["/{orgnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentOrganisasjon(@PathVariable orgnr: String): Ressurs<Organisasjon> {
        return success(data = organisasjonService.hentOrganisasjon(orgnr))
    }

}
