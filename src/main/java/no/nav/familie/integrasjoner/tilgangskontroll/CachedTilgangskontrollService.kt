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
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext.hentClaim
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext.hentJwt
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Service
class CachedTilgangskontrollService(
    private val egenAnsattService: EgenAnsattService,
    private val personopplysningerService: PersonopplysningerService,
    private val tilgangConfig: TilgangConfig,
) {
    @Cacheable(
        cacheNames = [TILGANG_TIL_BRUKER],
        keyGenerator = "tilgangCacheKeyGenerator",
        condition = "#personIdent != null",
    )
    fun sjekkTilgang(
        personIdent: String,
        tema: Tema,
    ): Tilgang =
        try {
            val adressebeskyttelse = personopplysningerService.hentAdressebeskyttelse(personIdent, tema).gradering
            hentTilgang(adressebeskyttelse, personIdent) { egenAnsattService.erEgenAnsatt(personIdent) }
        } catch (pdlUnauthorizedException: PdlUnauthorizedException) {
            Tilgang(personIdent = personIdent, harTilgang = false, begrunnelse = pdlUnauthorizedException.message)
        }

    @Cacheable(
        cacheNames = ["TILGANG_TIL_PERSON_MED_RELASJONER"],
        keyGenerator = "tilgangCacheKeyGenerator",
        condition = "#personIdent != null",
    )
    fun sjekkTilgangTilPersonMedRelasjoner(
        personIdent: String,
        tema: Tema,
    ): Tilgang {
        val personMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner(personIdent, tema)
        secureLogger.info("Sjekker tilgang til {}", personMedRelasjoner.personIdent)

        val høyesteGraderingen = TilgangskontrollUtil.høyesteGraderingen(personMedRelasjoner)
        return hentTilgang(høyesteGraderingen, personIdent) { erEgenAnsatt(personMedRelasjoner) }
    }

    private fun hentTilgang(
        adressebeskyttelsegradering: ADRESSEBESKYTTELSEGRADERING?,
        personIdent: String,
        egenAnsattSjekk: () -> Boolean,
    ): Tilgang {
        val tilgang =
            when (adressebeskyttelsegradering) {
                FORTROLIG -> {
                    hentTilgangForRolle(tilgangConfig.kode7, personIdent)
                }

                STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND -> {
                    hentTilgangForRolle(tilgangConfig.kode6, personIdent)
                }

                else -> {
                    Tilgang(personIdent = personIdent, harTilgang = true)
                }
            }
        if (!tilgang.harTilgang) {
            return tilgang
        }
        if (egenAnsattSjekk()) {
            return hentTilgangForRolle(tilgangConfig.egenAnsatt, personIdent)
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
        personIdent: String,
    ): Tilgang {
        val grupper = hentClaim<List<String>>("groups") ?: emptyList()
        if (grupper.any { it == adRolle?.rolleId }) {
            return Tilgang(personIdent, true)
        }
        secureLogger.info("${hentClaim<String>("preferred_username")} har ikke tilgang ${adRolle?.beskrivelse} for $personIdent")
        return Tilgang(personIdent = personIdent, harTilgang = false, begrunnelse = adRolle?.beskrivelse)
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")

        const val TILGANG_TIL_BRUKER = "tilgangTilBruker"
    }
}

@Configuration
class TilgangskontrollCacheConfig {
    @Bean
    fun tilgangCacheKeyGenerator(): KeyGenerator =
        KeyGenerator { _, _, params ->
            val personIdent = params[0] as String
            val subject = hentJwt().subject
            "$subject:$personIdent"
        }
}
