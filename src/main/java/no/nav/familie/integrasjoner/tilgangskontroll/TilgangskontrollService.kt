package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TilgangskontrollService(
    private val cachedTilgangskontrollService: CachedTilgangskontrollService,
) {
    fun sjekkTilgangTilBrukere(
        personIdenter: List<String>,
        tema: Tema = Tema.BAR,
    ): List<Tilgang> = personIdenter.map { ident -> sjekkTilgangTilBruker(ident, tema) }

    fun sjekkTilgangTilBruker(
        personIdent: String,
        tema: Tema = Tema.BAR,
    ): Tilgang {
        val jwtToken = SpringTokenValidationContextHolder().getTokenValidationContext().getJwtToken("azuread") ?: throw JwtTokenValidatorException("Klarte ikke hente token fra issuer azuread")
        return cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken, tema)
    }

    fun sjekkTilgang(
        personIdent: String,
        jwtToken: JwtToken,
        tema: Tema = Tema.BAR,
    ): Tilgang = cachedTilgangskontrollService.sjekkTilgang(personIdent, jwtToken, tema)

    fun sjekkTilgangTilPersonMedRelasjoner(
        personIdent: String,
        tema: Tema,
    ): Tilgang {
        if (tema != Tema.ENF && tema != Tema.BAR && tema != Tema.TSO) {
            throw IllegalArgumentException("Har ikke lagt inn støtte for andre enn ENF eller BAR")
        }
        val jwtToken = SpringTokenValidationContextHolder().getTokenValidationContext().getJwtToken("azuread") ?: throw JwtTokenValidatorException("Klarte ikke hente token fra issuer azuread")
        return cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(personIdent, jwtToken, tema)
    }
}
