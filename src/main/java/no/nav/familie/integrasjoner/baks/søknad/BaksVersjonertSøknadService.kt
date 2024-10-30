package no.nav.familie.integrasjoner.baks.søknad

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
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
) {
    fun hentBaksSøknadBase(
        journalpost: Journalpost,
        tema: Tema,
    ): BaksSøknadBase =
        when (tema) {
            Tema.KON -> hentVersjonertKontantstøtteSøknad(journalpost).kontantstøtteSøknad
            Tema.BAR -> hentVersjonertBarnetrygdSøknad(journalpost).barnetrygdSøknad
            else -> throw IllegalArgumentException("Støtter ikke deserialisering av søknad for tema $tema")
        }

    fun hentVersjonertBarnetrygdSøknad(
        journalpost: Journalpost,
    ): VersjonertBarnetrygdSøknad = objectMapper.readValue<VersjonertBarnetrygdSøknad>(hentSøknadJson(journalpost, Tema.BAR))

    fun hentVersjonertKontantstøtteSøknad(
        journalpost: Journalpost,
    ): VersjonertKontantstøtteSøknad = objectMapper.readValue<VersjonertKontantstøtteSøknad>(hentSøknadJson(journalpost, Tema.KON))

    private fun hentSøknadJson(
        journalpost: Journalpost,
        tema: Tema,
    ): String {
        val dokumentInfoId = journalpost.dokumenter!!.single { it.erDigitalSøknad(tema) }.dokumentInfoId
        return safHentDokumentRestClient.hentDokument(journalpostId = journalpost.journalpostId, dokumentInfoId = dokumentInfoId, variantFormat = Dokumentvariantformat.ORIGINAL.name).decodeToString()
    }
}
