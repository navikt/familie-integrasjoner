package no.nav.familie.integrasjoner.dokdistkanal

import no.nav.familie.integrasjoner.client.rest.DokdistkanalRestClient
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalRequest
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
class DokdistkanalController(private val dokdistkanalRestClient: DokdistkanalRestClient) {

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
        ).run {
            val distribusjonskanal = try {
                Distribusjonskanal.valueOf(distribusjonskanal)
            } catch (e: IllegalArgumentException) {
                logger.error("Distribusjonskanal-kontrakten er utdatert og må oppdateres med ny verdi for $distribusjonskanal")
                Distribusjonskanal.UKJENT
            }
            success(data = distribusjonskanal, melding = regelBegrunnelse)
        }
    }
}
