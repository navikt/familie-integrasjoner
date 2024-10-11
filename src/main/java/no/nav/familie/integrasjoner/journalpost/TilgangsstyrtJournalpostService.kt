package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.mottak.MottakService
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import org.springframework.stereotype.Service

@Service
class TilgangsstyrtJournalpostService(
    private val mottakService: MottakService,
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
        return if (tema != null && journalpost.erDigitalSøknad(tema)) {
            harTilgangTilDigitalSøknad(journalpost = journalpost, tema = tema)
        } else {
            // Vi har kun mulighet til å sjekke tilganger for digitale søknader, da innholdet i papirsøknader er ukjent.
            true
        }
    }

    private fun harTilgangTilDigitalSøknad(
        journalpost: Journalpost,
        tema: Tema,
    ): Boolean {
        val personerIDigitalSøknad = mottakService.hentPersonerIDigitalSøknad(tema = tema, journalpostId = journalpost.journalpostId)
        return tilgangskontrollService.sjekkTilgangTilBrukere(personIdenter = personerIDigitalSøknad, tema = tema).all { tilgang -> tilgang.harTilgang }
    }
}
