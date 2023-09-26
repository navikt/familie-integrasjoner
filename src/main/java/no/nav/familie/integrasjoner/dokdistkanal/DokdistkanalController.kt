package no.nav.familie.integrasjoner.dokdistkanal

import no.nav.familie.integrasjoner.client.rest.DokdistkanalRestClient
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dokdistkanal")
class DokdistkanalController(private val dokdistkanalRestClient: DokdistkanalRestClient) {

    @PostMapping(path = ["/{tema}"])
    fun hentDistribusjonskanal(
        @RequestBody request: DokdistkanalRequest,
        @PathVariable tema: Tema,
    ): Ressurs<String> {
        return dokdistkanalRestClient.bestemDistribusjonskanal(
            BestemDistribusjonskanalRequest(
                brukerId = request.bruker.ident,
                mottakerId = request.mottaker.ident,
                tema = tema,
                dokumenttypeId = request.dokumenttypeId,
                erArkivert = request.erArkivert,
                forsendelseStørrelse = request.forsendelseStørrelse,
            ),
        ).run { success(data = distribusjonskanal, melding = regelBegrunnelse) }
    }
}

data class DokdistkanalRequest(
    val bruker: PersonIdent,
    val mottaker: PersonIdent,
    val dokumenttypeId: String? = null,
    val erArkivert: Boolean? = null,
    val forsendelseStørrelse: Int? = null,
)
