package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull

@RestController @RequestMapping(value = ["/api/tilgang"])
class TilgangskontrollController @Autowired constructor(private val azureGraphRestClient: AzureGraphRestClient,
                                                        private val tilgangService: TilgangsKontrollService,
                                                        private val personService: PersonopplysningerService) {

    @GetMapping(path = ["/person"]) @ProtectedWithClaims(issuer = "azuread") fun tilgangTilPerson(
            @RequestHeader(name = "Nav-Personident")
            personIdent: @NotNull String?): ResponseEntity<Tilgang?> {
        return sjekkTilgangTilBruker(personIdent)
    }

    private fun sjekkTilgangTilBruker(personIdent: String?): ResponseEntity<Tilgang?> {
        val saksbehandler = azureGraphRestClient.saksbehandler
        val personInfo = personService.hentPersoninfo(personIdent)
        val tilgang = tilgangService.sjekkTilgang(personIdent, saksbehandler, personInfo)
        return lagRespons(tilgang)
    }

    private fun lagRespons(tilgang: Tilgang?): ResponseEntity<Tilgang?> {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tilgang)
    }

}
