package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRoller
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(private val azureGraphRestClient: AzureGraphRestClient,
                              private val egenAnsattService: EgenAnsattService,
                              private val personopplysningerService: PersonopplysningerService) {

    fun sjekkTilgangTilBrukere(personIdenter: List<String>): List<Tilgang> {
        return personIdenter.map(this::sjekkTilgangTilBruker)
    }

    fun sjekkTilgangTilBruker(personIdent: String): Tilgang {
        val saksbehandler = azureGraphRestClient.saksbehandler
        val personInfo = personopplysningerService.hentPersoninfo(personIdent)
        return sjekkTilgang(personIdent, saksbehandler, personInfo)
    }

    @Cacheable(cacheNames = [TILGANGTILBRUKER],
               key = "#saksbehandler.onPremisesSamAccountName.concat(#personFnr)",
               condition = "#personFnr != null && #saksbehandler.onPremisesSamAccountName != null")
    fun sjekkTilgang(personFnr: String, saksbehandler: Saksbehandler, personInfo: Personinfo): Tilgang {
        val navIdent = saksbehandler.onPremisesSamAccountName
        val diskresjonskode = personInfo.diskresjonskode
        logger.info("$navIdent har tilgang til: $saksbehandler")

        if (DISKRESJONSKODE_KODE6 == diskresjonskode && !harTilgangTilKode6(saksbehandler)) {
//            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            logger.info("$navIdent har ikke tilgang til ${AdRoller.KODE6.rolle}. Saksbehandler: $saksbehandler")

            return Tilgang(false, AdRoller.KODE6.rolle)
        }
        if (DISKRESJONSKODE_KODE7 == diskresjonskode && !harTilgangTilKode7(saksbehandler)) {
//            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            logger.info("$navIdent har ikke tilgang til ${AdRoller.KODE7.rolle}. Saksbehandler: $saksbehandler")

            return Tilgang(false, AdRoller.KODE7.rolle)
        }
        if (egenAnsattService.erEgenAnsatt(personFnr) && !harTilgangTilEgenAnsatt(saksbehandler)) {
//            secureLogger.info("$navIdent har ikke tilgang til egen ansatt $personFnr")
            logger.info("$navIdent har ikke tilgang til ${AdRoller.KODE7.rolle}. Saksbehandler: $saksbehandler")

            return Tilgang(false, AdRoller.EGEN_ANSATT.rolle)
        }
        return Tilgang(true)
    }

    private fun harTilgangTilKode7(saksbehandler: Saksbehandler): Boolean {
        return saksbehandler.grupper.any { it.onPremisesSamAccountName == AdRoller.KODE7.rolle }
    }

    private fun harTilgangTilKode6(saksbehandler: Saksbehandler): Boolean {
        return saksbehandler.grupper.any { it.onPremisesSamAccountName == AdRoller.KODE6.rolle }
    }

    private fun harTilgangTilEgenAnsatt(saksbehandler: Saksbehandler): Boolean {
        return saksbehandler.grupper.any { it.onPremisesSamAccountName == AdRoller.EGEN_ANSATT.rolle }
    }

    companion object {
        const val GEOGRAFISK = "GEOGRAFISK"
        const val TILGANGTILBRUKER = "tilgangtilbruker"
        const val TILGANGTILTJENESTEN = "tilgangtiltjenesten"
        const val TILGANGTILENHET = "tilgangtilenhet"
        private const val ENHET = "ENHET"
        const val DISKRESJONSKODE_KODE6 = "SPSF"
        const val DISKRESJONSKODE_KODE7 = "SPFO"
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
        private val logger = LoggerFactory.getLogger(TilgangskontrollService::class.java)
    }
}
