package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import no.nav.familie.integrasjoner.client.rest.AzureGraphRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
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
        val saksbehandler = azureGraphRestClient.hentSaksbehandler()
        val personInfo = personopplysningerService.hentPersoninfo(personIdent)
        return sjekkTilgang(personIdent, saksbehandler, personInfo)
    }

    @Cacheable(cacheNames = [TILGANG_TIL_BRUKER],
               key = "#saksbehandler.id.concat(#personFnr)",
               condition = "#personFnr != null && #saksbehandler.id != null")
    fun sjekkTilgang(personFnr: String, saksbehandler: Saksbehandler, personInfo: Personinfo): Tilgang {
        val navIdent = saksbehandler.userPrincipalName
        val diskresjonskode = personInfo.diskresjonskode

        if (DISKRESJONSKODE_KODE6 == diskresjonskode && !harRolleForTilgang(AdRolle.KODE6)) {
            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            return Tilgang(false, AdRolle.KODE6.rollekode)
        }

        if (DISKRESJONSKODE_KODE7 == diskresjonskode && !harRolleForTilgang(AdRolle.KODE7)) {
            secureLogger.info("$navIdent har ikke tilgang til $personFnr")
            return Tilgang(false, AdRolle.KODE7.rollekode)
        }

        if (egenAnsattService.erEgenAnsatt(personFnr) && !harRolleForTilgang(AdRolle.EGEN_ANSATT)) {
            secureLogger.info("$navIdent har ikke tilgang til egen ansatt $personFnr")
            return Tilgang(false, AdRolle.EGEN_ANSATT.rollekode)
        }
        return Tilgang(true)
    }

    private fun harRolleForTilgang(adRolle: AdRolle): Boolean {
        return azureGraphRestClient.hentGrupper().value.any { it.onPremisesSamAccountName == adRolle.rollekode }
    }

    companion object {
        const val TILGANG_TIL_BRUKER = "tilgangTilBruker"
        const val DISKRESJONSKODE_KODE6 = "SPSF"
        const val DISKRESJONSKODE_KODE7 = "SPFO"
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
