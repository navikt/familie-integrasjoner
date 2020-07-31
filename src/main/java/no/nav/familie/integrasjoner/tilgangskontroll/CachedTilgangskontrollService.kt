package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedTilgangskontrollService(private val egenAnsattService: EgenAnsattService,
                                    private val personopplysningerService: PersonopplysningerService,
                                    private val tilgangConfig: TilgangConfig) {

    @Cacheable(cacheNames = [TILGANG_TIL_BRUKER],
               key = "#jwtToken.subject.concat(#personIdent)",
               condition = "#personIdent != null && #jwtToken.subject != null")
    fun sjekkTilgang(personIdent: String, jwtToken: JwtToken): Tilgang {
        val personInfo = personopplysningerService.hentPersoninfo(personIdent, "BAR", PersonInfoQuery.ENKEL)
        val adressebeskyttelse = personInfo.adressebeskyttelseGradering

        if (egenAnsattService.erEgenAnsatt(personIdent)) {
            return hentTilgangForRolle(tilgangConfig.grupper["utvidetTilgang"], jwtToken, personIdent)
        }

        return when (adressebeskyttelse) {
            ADRESSEBESKYTTELSEGRADERING.FORTROLIG -> hentTilgangForRolle(tilgangConfig.grupper["kode7"], jwtToken, personIdent)
            ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG, ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND ->
                hentTilgangForRolle(
                    tilgangConfig.grupper["kode6"],
                    jwtToken,
                    personIdent)
            else -> Tilgang(true)
        }
    }

    private fun hentTilgangForRolle(adRolle: AdRolle?, jwtToken: JwtToken, personIdent: String): Tilgang {
        val grupper = jwtToken.jwtTokenClaims.getAsList("groups")
        if (grupper.any { it == adRolle?.rolleId }) {
            return Tilgang(true)
        }
        secureLogger.info("${jwtToken.jwtTokenClaims["preferred_username"]} " +
                          "har ikke tilgang ${adRolle?.beskrivelse} for $personIdent")
        return Tilgang(false, adRolle?.beskrivelse)
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")

        const val TILGANG_TIL_BRUKER = "tilgangTilBruker"
    }
}