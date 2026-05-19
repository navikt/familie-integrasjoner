package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.tilgangskontroll.Tilgang
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
    ): Tilgang = cachedTilgangskontrollService.sjekkTilgang(personIdent, tema)

    fun sjekkTilgang(
        personIdent: String,
        tema: Tema = Tema.BAR,
    ): Tilgang = cachedTilgangskontrollService.sjekkTilgang(personIdent, tema)

    fun sjekkTilgangTilPersonMedRelasjoner(
        personIdent: String,
        tema: Tema,
    ): Tilgang {
        if (tema != Tema.ENF && tema != Tema.BAR && tema != Tema.TSO) {
            throw IllegalArgumentException("Har ikke lagt inn støtte for andre enn ENF, BAR eller TSO")
        }
        return cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(personIdent, tema)
    }
}
