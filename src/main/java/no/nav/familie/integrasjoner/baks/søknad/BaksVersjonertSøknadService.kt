package no.nav.familie.integrasjoner.baks.søknad

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.ba.søknad.VersjonertBarnetrygdSøknad
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.søknad.BaksSøknadBase
import no.nav.familie.kontrakter.ks.søknad.VersjonertKontantstøtteSøknad
import org.springframework.stereotype.Service

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

    fun hentBarnetrygdSøknadBase(journalpost: Journalpost): BaksSøknadBase = objectMapper.readValue<VersjonertBarnetrygdSøknad>(hentJsonSøknadFraJournalpost(journalpost, Tema.BAR)).barnetrygdSøknad

    fun hentKontantstøtteSøknadBase(journalpost: Journalpost): BaksSøknadBase = objectMapper.readValue<VersjonertKontantstøtteSøknad>(hentJsonSøknadFraJournalpost(journalpost, Tema.KON)).kontantstøtteSøknad

    fun hentVersjonertBarnetrygdSøknad(
        journalpostId: String,
    ): VersjonertBarnetrygdSøknad = objectMapper.readValue<VersjonertBarnetrygdSøknad>(hentJsonSøknadFraJournalpostId(journalpostId, Tema.BAR))

    fun hentVersjonertKontantstøtteSøknad(
        journalpostId: String,
    ): VersjonertKontantstøtteSøknad = objectMapper.readValue<VersjonertKontantstøtteSøknad>(hentJsonSøknadFraJournalpostId(journalpostId, Tema.KON))

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
        val dokumentInfoId = journalpost.dokumenter!!.single { it.erSøknadForTema(tema) }.dokumentInfoId
        return safHentDokumentRestClient.hentDokument(journalpostId = journalpost.journalpostId, dokumentInfoId = dokumentInfoId, variantFormat = Dokumentvariantformat.ORIGINAL.name).decodeToString()
    }
}
