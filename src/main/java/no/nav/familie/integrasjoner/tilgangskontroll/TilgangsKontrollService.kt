package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRoller
import no.nav.familie.integrasjoner.tilgangskontroll.domene.Tilgang
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TilgangsKontrollService @Autowired internal constructor(private val azureGraphRestClient: AzureGraphRestClient,
                                                              private val egenAnsattService: EgenAnsattService) {

    @Cacheable(cacheNames = [TILGANGTILBRUKER],
               key = "#saksbehandler.onPremisesSamAccountName.concat(#personFnr)",
               condition = "#personFnr != null && #saksbehandler.onPremisesSamAccountName != null")
    fun sjekkTilgang(personFnr: String?, saksbehandler: Saksbehandler, personInfo: Personinfo): Tilgang? {
        val NAVident = saksbehandler.onPremisesSamAccountName
        val diskresjonskode = personInfo.diskresjonskode
        if (DISKRESJONSKODE_KODE6 == diskresjonskode && !harTilgangTilKode6(NAVident)) {
            secureLogger.info("$NAVident har ikke tilgang til $personFnr")
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.KODE6.name)
        }
        if (DISKRESJONSKODE_KODE7 == diskresjonskode && !harTilgangTilKode7(NAVident)) {
            secureLogger.info("$NAVident har ikke tilgang til $personFnr")
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.KODE7.name)
        }
        if (egenAnsattService.erEgenAnsatt(personFnr) && !harTilgangTilEgenAnsatt(NAVident)) {
            secureLogger.info("$NAVident har ikke tilgang til egen ansatt $personFnr")
            return Tilgang().withHarTilgang(false).withBegrunnelse(AdRoller.EGEN_ANSATT.name)
        }
        return Tilgang().withHarTilgang(true)
    }

    private fun harTilgangTilKode7(NAVident: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(NAVident, KODE7.rolle);
    }

    private fun harTilgangTilKode6(NAVident: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(NAVident, KODE6.rolle);
    }

    private fun harTilgangTilEgenAnsatt(NAVident: String?): Boolean {
        return false
        //TODO hent roller fra token eller hent fra graph api til azuread
//return ldapService.harTilgang(NAVident, EGEN_ANSATT.rolle);
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
        private val LOG = LoggerFactory.getLogger(TilgangsKontrollService::class.java)
    }

}
