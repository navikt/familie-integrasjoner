package no.nav.familie.integrasjoner.dokdistkanal

import no.nav.familie.integrasjoner.client.rest.DokdistkanalRestClient
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokdistkanal.Distribusjonskanal
import no.nav.familie.kontrakter.felles.dokdistkanal.DokdistkanalRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dokdistkanal")
class DokdistkanalController(
    private val dokdistkanalRestClient: DokdistkanalRestClient,
    private val personopplysningerService: PersonopplysningerService,
) {

    private val logger: Logger = LoggerFactory.getLogger(DokdistkanalController::class.java)

    @PostMapping(path = ["/{tema}"])
    fun hentDistribusjonskanal(
        @RequestBody request: DokdistkanalRequest,
        @PathVariable tema: Tema,
    ): Ressurs<Distribusjonskanal> {
        return dokdistkanalRestClient.bestemDistribusjonskanal(
            BestemDistribusjonskanalRequest(
                brukerId = request.bruker.ident,
                mottakerId = request.mottaker.ident,
                tema = tema,
                dokumenttypeId = request.dokumenttypeId,
                erArkivert = request.erArkivert,
                forsendelseStørrelse = request.forsendelseStørrelseIMegabytes,
            ),
        ).valider(request.mottaker, tema) { distribusjonskanal, melding ->
            success(data = distribusjonskanal, melding = melding)
        }
    }

    private fun BestemDistribusjonskanalResponse.valider(
        mottaker: PersonIdent,
        tema: Tema,
        valid: (Distribusjonskanal, String) -> Ressurs<Distribusjonskanal>,
    ): Ressurs<Distribusjonskanal> {
        var distribusjonskanal = try {
            Distribusjonskanal.valueOf(distribusjonskanal)
        } catch (e: IllegalArgumentException) {
            logger.error("Distribusjonskanal-kontrakten er utdatert og må oppdateres med ny verdi for $distribusjonskanal")
            Distribusjonskanal.UKJENT
        }
        var melding = regelBegrunnelse

        if (distribusjonskanal == Distribusjonskanal.PRINT && !mottaker.harPostadresse(tema)) {
            distribusjonskanal = Distribusjonskanal.INGEN_DISTRIBUSJON
            melding = "Mottaker har ukjent adresse"
        }
        return valid(distribusjonskanal, melding)
    }

    private fun PersonIdent.harPostadresse(tema: Tema): Boolean {
        val postadresse = personopplysningerService.hentPostadresse(ident, tema)
        return postadresse != null
    }
}
