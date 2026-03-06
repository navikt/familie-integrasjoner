package no.nav.familie.integrasjoner.journalpost.versjonertsøknad

import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.journalpost.JournalpostNotFoundException
import no.nav.familie.kontrakter.ba.søknad.VersjonertBarnetrygdSøknad
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.søknad.BaksSøknadBase
import no.nav.familie.kontrakter.ks.søknad.VersjonertKontantstøtteSøknad
import org.springframework.stereotype.Service
import tools.jackson.module.kotlin.readValue

@Service
class BaksVersjonertSøknadService(
    private val safHentDokumentRestClient: SafHentDokumentRestClient,
    private val safRestClient: SafRestClient,
) {
    fun hentBaksSøknadBase(
        journalpost: Journalpost,
        tema: Tema,
    ): BaksSøknadBase =
        when (tema) {
            Tema.KON -> hentKontantstøtteSøknadBase(journalpost)
            Tema.BAR -> hentBarnetrygdSøknadBase(journalpost)
            else -> throw IllegalArgumentException("Støtter ikke deserialisering av søknad for tema $tema")
        }

    fun hentBarnetrygdSøknadBase(journalpost: Journalpost): BaksSøknadBase = jsonMapper.readValue<VersjonertBarnetrygdSøknad>(hentJsonSøknadFraJournalpost(journalpost, Tema.BAR)).barnetrygdSøknad

    fun hentKontantstøtteSøknadBase(journalpost: Journalpost): BaksSøknadBase = jsonMapper.readValue<VersjonertKontantstøtteSøknad>(hentJsonSøknadFraJournalpost(journalpost, Tema.KON)).kontantstøtteSøknad

    fun hentVersjonertBarnetrygdSøknad(
        journalpostId: String,
    ): VersjonertBarnetrygdSøknad = jsonMapper.readValue<VersjonertBarnetrygdSøknad>(hentJsonSøknadFraJournalpostId(journalpostId, Tema.BAR))

    fun hentVersjonertKontantstøtteSøknad(
        journalpostId: String,
    ): VersjonertKontantstøtteSøknad = jsonMapper.readValue<VersjonertKontantstøtteSøknad>(hentJsonSøknadFraJournalpostId(journalpostId, Tema.KON))

    private fun hentJsonSøknadFraJournalpostId(
        journalpostId: String,
        tema: Tema,
    ): String {
        val journalpost = safRestClient.hentJournalpost(journalpostId)
        return hentJsonSøknadFraJournalpost(journalpost, tema)
    }

    private fun hentJsonSøknadFraJournalpost(
        journalpost: Journalpost,
        tema: Tema,
    ): String {
        val dokumentInfoId = journalpost.dokumenter?.firstOrNull { it.erSøknadForTema(tema) }?.dokumentInfoId
        dokumentInfoId ?: throw JournalpostNotFoundException("Fant ikke dokumenter for tema $tema i journalpost", journalpostId = journalpost.journalpostId)

        return safHentDokumentRestClient.hentDokument(journalpostId = journalpost.journalpostId, dokumentInfoId = dokumentInfoId, variantFormat = Dokumentvariantformat.ORIGINAL.name).decodeToString()
    }
}
