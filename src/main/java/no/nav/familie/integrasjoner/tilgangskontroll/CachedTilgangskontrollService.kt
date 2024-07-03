package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PdlUnauthorizedException
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedTilgangskontrollService(
    private val egenAnsattService: EgenAnsattService,
    private val personopplysningerService: PersonopplysningerService,
    private val tilgangConfig: TilgangConfig,
) {
    @Cacheable(
        cacheNames = [TILGANG_TIL_BRUKER],
        key = "#jwtToken.subject.concat(#personIdent)",
        condition = "#personIdent != null && #jwtToken.subject != null",
    )
    fun sjekkTilgang(
        personIdent: String,
        jwtToken: JwtToken,
        tema: Tema,
    ): Tilgang {
        return try {
            val adressebeskyttelse = personopplysningerService.hentAdressebeskyttelse(personIdent, tema).gradering
            hentTilgang(adressebeskyttelse, jwtToken, personIdent) { egenAnsattService.erEgenAnsatt(personIdent) }
        } catch (pdlUnauthorizedException: PdlUnauthorizedException) {
            Tilgang(personIdent = personIdent, harTilgang = false)
        }
    }

    @Cacheable(
        cacheNames = ["TILGANG_TIL_PERSON_MED_RELASJONER"],
        key = "#jwtToken.subject.concat(#personIdent)",
        condition = "#jwtToken.subject != null",
    )
    fun sjekkTilgangTilPersonMedRelasjoner(
        personIdent: String,
        jwtToken: JwtToken,
        tema: Tema,
    ): Tilgang {
        val personMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner(personIdent, tema)
        secureLogger.info("Sjekker tilgang til {}", personMedRelasjoner)

        val høyesteGraderingen = TilgangskontrollUtil.høyesteGraderingen(personMedRelasjoner)
        return hentTilgang(høyesteGraderingen, jwtToken, personIdent) { erEgenAnsatt(personMedRelasjoner) }
    }

    private fun hentTilgang(
        adressebeskyttelsegradering: ADRESSEBESKYTTELSEGRADERING?,
        jwtToken: JwtToken,
        personIdent: String,
        egenAnsattSjekk: () -> Boolean,
    ): Tilgang {
        val tilgang =
            when (adressebeskyttelsegradering) {
                FORTROLIG -> hentTilgangForRolle(tilgangConfig.kode7, jwtToken, personIdent)
                STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND ->
                    hentTilgangForRolle(tilgangConfig.kode6, jwtToken, personIdent)

                else -> Tilgang(personIdent = personIdent, harTilgang = true)
            }
        if (!tilgang.harTilgang) {
            return tilgang
        }
        if (egenAnsattSjekk()) {
            return hentTilgangForRolle(tilgangConfig.egenAnsatt, jwtToken, personIdent)
        }
        return Tilgang(personIdent = personIdent, harTilgang = true)
    }

    /**
     * Vi ønsker å sjekke om person med relasjoner er egenAnsatt.
     * Dette gjelder personen vi jobber med, barn (voksne barn er også med), personer relatert via sivilstand (gift med, separtert fra)
     * og barnets andre foreldrer.
     */
    private fun erEgenAnsatt(personMedRelasjoner: PersonMedRelasjoner): Boolean {
        val relevanteIdenter =
            setOf(personMedRelasjoner.personIdent) +
                personMedRelasjoner.sivilstand.map { it.personIdent } +
                personMedRelasjoner.barn.map { it.personIdent } +
                personMedRelasjoner.barnsForeldrer.map { it.personIdent }

        return egenAnsattService.erEgenAnsatt(relevanteIdenter).any { it.value }
    }

    private fun hentTilgangForRolle(
        adRolle: AdRolle?,
        jwtToken: JwtToken,
        personIdent: String,
    ): Tilgang {
        val grupper = jwtToken.jwtTokenClaims.getAsList("groups")
        if (grupper.any { it == adRolle?.rolleId }) {
            return Tilgang(personIdent, true)
        }
        secureLogger.info(
            "${jwtToken.jwtTokenClaims.get("preferred_username")} " +
                "har ikke tilgang ${adRolle?.beskrivelse} for $personIdent",
        )
        return Tilgang(personIdent = personIdent, harTilgang = false, begrunnelse = adRolle?.beskrivelse)
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")

        const val TILGANG_TIL_BRUKER = "tilgangTilBruker"
    }
}
