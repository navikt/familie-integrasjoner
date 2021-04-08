package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.felles.Tema
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
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
    fun sjekkTilgang(personIdent: String, jwtToken: JwtToken, tema: Tema): Tilgang {
        val personInfo = personopplysningerService.hentPersoninfo(personIdent, tema.toString(), PersonInfoQuery.ENKEL)
        val adressebeskyttelse = personInfo.adressebeskyttelseGradering

        if (egenAnsattService.erEgenAnsatt(personIdent)) {
            return hentTilgangForRolle(tilgangConfig.grupper["utvidetTilgang"], jwtToken, personIdent)
        }

        return when (adressebeskyttelse) {
            FORTROLIG -> hentTilgangForRolle(tilgangConfig.grupper["kode7"], jwtToken, personIdent)
            STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND ->
                hentTilgangForRolle(
                        tilgangConfig.grupper["kode6"],
                        jwtToken,
                        personIdent)
            else -> Tilgang(true)
        }
    }

    @Cacheable(cacheNames = ["TILGANG_TIL_PERSON_MED_RELASJONER"],
               key = "#jwtToken.subject.concat(#personIdent)",
               condition = "#jwtToken.subject != null")
    fun sjekkTilgangTilPersonMedRelasjoner(personIdent: String, jwtToken: JwtToken, tema: Tema): Tilgang {
        val personMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner(personIdent, tema)

        val tilgang = when (høyesteGraderingen(personMedRelasjoner)) {
            FORTROLIG -> hentTilgangForRolle(tilgangConfig.grupper["kode7"], jwtToken, personIdent)
            STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND ->
                hentTilgangForRolle(tilgangConfig.grupper["kode6"], jwtToken, personIdent)
            else -> Tilgang(harTilgang = true)
        }
        if (!tilgang.harTilgang) {
            return tilgang
        }
        if (erEgenAnsatt(personMedRelasjoner)) {
            return hentTilgangForRolle(tilgangConfig.grupper["utvidetTilgang"], jwtToken, personIdent)
        }
        return Tilgang(harTilgang = true)
    }

    /**
     * Trenger kun å sjekke personen og barnets andre foreldrer for om de er ansatt
     */
    private fun erEgenAnsatt(personMedRelasjoner: PersonMedRelasjoner): Boolean {
        val relevanteIdenter = setOf(personMedRelasjoner.personIdent) + personMedRelasjoner.barnsForeldrer.map { it.personIdent }
        return egenAnsattService.erEgenAnsatt(relevanteIdenter).any { it.value }
    }

    private fun høyesteGraderingen(personUtvidet: PersonMedRelasjoner): ADRESSEBESKYTTELSEGRADERING? {
        val adressebeskyttelser =
                (personUtvidet.adressebeskyttelse?.let { setOf(it) } ?: emptySet<ADRESSEBESKYTTELSEGRADERING>() +
                 listOf(personUtvidet.sivilstand, personUtvidet.fullmakt, personUtvidet.barn, personUtvidet.barnsForeldrer)
                         .flatMap { relasjoner -> relasjoner.mapNotNull { it.adressebeskyttelse } })
        return when {
            adressebeskyttelser.contains(STRENGT_FORTROLIG_UTLAND) -> STRENGT_FORTROLIG_UTLAND
            adressebeskyttelser.contains(STRENGT_FORTROLIG) -> STRENGT_FORTROLIG
            adressebeskyttelser.contains(FORTROLIG) -> FORTROLIG
            else -> null
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