package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.baks.søknad.BaksVersjonertSøknadService
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import no.nav.familie.kontrakter.felles.søknad.MissingVersionException
import no.nav.familie.kontrakter.felles.søknad.UnsupportedVersionException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BaksTilgangsstyrtJournalpostService(
    private val baksVersjonertSøknadService: BaksVersjonertSøknadService,
    private val tilgangskontrollService: TilgangskontrollService,
) {
    fun mapTilTilgangsstyrteJournalposter(journalposter: List<Journalpost>): List<TilgangsstyrtJournalpost> =
        journalposter.map { journalpost ->
            TilgangsstyrtJournalpost(
                journalpost = journalpost,
                harTilgang = harTilgangTilJournalpost(journalpost = journalpost),
            )
        }

    fun harTilgangTilJournalpost(journalpost: Journalpost): Boolean {
        val tema = journalpost.tema?.let { tema -> Tema.valueOf(tema) }
        if (tema == null) {
            return true
        }

        if (!støtterTilgangsstyringSjekk(journalpost, tema)) return true

        return try {
            val baksSøknadBase = baksVersjonertSøknadService.hentBaksSøknadBase(journalpost, tema)
            tilgangskontrollService
                .sjekkTilgangTilBrukere(
                    personIdenter = baksSøknadBase.personerISøknad(),
                    tema = tema,
                ).all { tilgang -> tilgang.harTilgang }
        } catch (e: MissingVersionException) {
            logger.warn("Får ikke sjekket tilgang til digital søknad tilknyttet journalpost ${journalpost.journalpostId}.")
            secureLogger.warn("Feil ved deserialisering av digital søknad.", e)
            // For gamle søknader, før 'kontraktVersjon' ble innført, har vi ingen måte å bestemme konkret klasse å deserialisere til.
            // Gir tilgang basert på antagelsen om at fagsak relatert til gamle søknader allerede er satt til Vikafossen dersom det finnes kode 6, 7 eller 19 personer i søknad.
            true
        } catch (e: UnsupportedVersionException) {
            logger.error("Får ikke sjekket tilgang til digital søknad tilknyttet journalpost ${journalpost.journalpostId}, da vi mangler støtte for kontraktversjon.")
            secureLogger.error("Feil ved deserialisering av digital søknad.", e)
            // Hindrer tilgang og logger Error da vi burde kunne deserialisere alle baks-søknader med feltet `kontraktVersjon`.
            false
        }
    }

    private fun støtterTilgangsstyringSjekk(
        journalpost: Journalpost,
        tema: Tema,
    ): Boolean {
        val datoMottatt = journalpost.datoMottatt ?: return false
        if (!journalpost.harDigitalSøknad(tema)) return false
        val harDigitalSøknadForTemaMedOriginalVariant = journalpost.harDigitalSøknad(tema) && journalpost.dokumenter!!.any { it.erSøknadForTema(tema) && it.harOriginalVariant() }

        val tidligsteStøtteForTilgangsstyrtDokument =
            when (tema) {
                Tema.KON -> tidligsteStøtteForTilgangsstyrtDokumentForKontantstøtte
                Tema.BAR -> tidligsteStøtteForTilgangsstyrtDokumentForBarnetrygd
                else -> error("Støtter ikke tilgangsstyringssjekk for tema: $tema")
            }

        if (tidligsteStøtteForTilgangsstyrtDokument < datoMottatt && !harDigitalSøknadForTemaMedOriginalVariant) {
            logger.warn("Journalpost ${journalpost.journalpostId} for digital søknad som burde hatt en original variant mangler dette. Kjører ingen tilgangssjekk.")
        }

        return harDigitalSøknadForTemaMedOriginalVariant
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaksTilgangsstyrtJournalpostService::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
        private val tidligsteStøtteForTilgangsstyrtDokumentForKontantstøtte = LocalDateTime.of(2022, 12, 13, 0, 0)
        private val tidligsteStøtteForTilgangsstyrtDokumentForBarnetrygd = LocalDateTime.of(2020, 7, 21, 0, 0)
    }
}
