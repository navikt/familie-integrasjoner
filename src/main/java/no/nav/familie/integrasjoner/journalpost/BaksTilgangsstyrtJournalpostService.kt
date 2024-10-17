package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.baks.søknad.BaksVersjonertSøknadService
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import no.nav.familie.kontrakter.felles.søknad.MissingVersionImplementationException
import no.nav.familie.kontrakter.felles.søknad.UnsupportedVersionException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BaksTilgangsstyrtJournalpostService(
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService,
    private val tilgangskontrollService: TilgangskontrollService,
) {
    fun tilTilgangstyrteJournalposter(journalposter: List<Journalpost>): List<TilgangsstyrtJournalpost> =
        journalposter.map { journalpost ->
            TilgangsstyrtJournalpost(
                journalpost = journalpost,
                harTilgang = harTilgangTilJournalpost(journalpost = journalpost),
            )
        }

    private fun harTilgangTilJournalpost(journalpost: Journalpost): Boolean {
        val tema = journalpost.tema?.let { tema -> Tema.valueOf(tema) }
        return if (tema != null && journalpost.harDigitalSøknad(tema)) {
            try {
                val versjonertSøknad = baksVersjonertSøknadService.hentBaksSøknadBase(journalpost, tema)
                tilgangskontrollService
                    .sjekkTilgangTilBrukere(
                        personIdenter = versjonertSøknad.personerISøknad(),
                        tema = tema,
                    ).all { tilgang -> tilgang.harTilgang }
            } catch (e: UnsupportedVersionException) {
                logger.warn("Får ikke sjekket tilgang til digital søknad tilknyttet journalpost ${journalpost.journalpostId}.")
                secureLogger.warn("Feil ved deserialisering av digital søknad.", e)
                // For gamle søknader, før 'kontraktVersjon' ble innført, har vi ingen måte å bestemme konkret klasse å deserialisere til.
                // Gir tilgang basert på antagelsen om at fagsak relatert til gamle søknader allerede er satt til Vikafossen dersom det finnes kode 6, 7 eller 19 personer i søknad.
                true
            } catch (e: MissingVersionImplementationException) {
                logger.error("Får ikke sjekket tilgang til digital søknad tilknyttet journalpost ${journalpost.journalpostId}, da vi mangler støtte for kontraktversjon.")
                secureLogger.error("Feil ved deserialisering av digital søknad.", e)
                // Hindrer tilgang og logger Error da vi burde kunne deserialisere alle baks-søknader med feltet `kontraktVersjon`.
                false
            }
        } else {
            // Vi har kun mulighet til å sjekke tilganger for digitale søknader, da innholdet i papirsøknader er ukjent.
            true
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaksTilgangsstyrtJournalpostService::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
