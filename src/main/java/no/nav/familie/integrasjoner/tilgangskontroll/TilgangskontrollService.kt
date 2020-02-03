package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRoller
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(private val azureGraphRestClient: AzureGraphRestClient,
                                                              private val egenAnsattService: EgenAnsattService) {

    @Cacheable(cacheNames = [TILGANGTILBRUKER],
               key = "#saksbehandler.onPremisesSamAccountName.concat(#personFnr)",
               condition = "#personFnr != null && #saksbehandler.onPremisesSamAccountName != null")
    fun sjekkTilgang(personFnr: String, saksbehandler: Saksbehandler, personInfo: Personinfo): Tilgang {
        val navIdent = saksbehandler.onPremisesSamAccountName
        val diskresjonskode = personInfo.diskresjonskode

        if (DISKRESJONSKODE_KODE6 == diskresjonskode && !harTilgangTilKode6(navIdent)) {
            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            return Tilgang(false, AdRoller.KODE6.name)
        }
        if (DISKRESJONSKODE_KODE7 == diskresjonskode && !harTilgangTilKode7(navIdent)) {
            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            return Tilgang(false, AdRoller.KODE7.name)
        }
        if (egenAnsattService.erEgenAnsatt(personFnr) && !harTilgangTilEgenAnsatt(navIdent)) {
            secureLogger.info("$navIdent har ikke tilgang til egen ansatt $personFnr")
            return Tilgang(false, AdRoller.EGEN_ANSATT.name)
        }
        return Tilgang(true)
    }

    private fun harTilgangTilKode7(navIdent: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(navIdent, KODE7.rolle);
    }

    private fun harTilgangTilKode6(navIdent: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(navIdent, KODE6.rolle);
    }

    private fun harTilgangTilEgenAnsatt(navIdent: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(navIdent, EGEN_ANSATT.rolle);
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
        private val LOG = LoggerFactory.getLogger(TilgangskontrollService::class.java)
    }

}
