package no.nav.familie.integrasjoner.journalpost.selvbetjening

import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import no.nav.familie.integrasjoner.safselvbetjening.generated.hentdokumentoversikt.Dokumentoversikt
import org.springframework.stereotype.Service

@Service
class SafSelvbetjeningService(
    private val safSelvbetjeningClient: SafSelvbetjeningClient,
) {
    suspend fun hentDokumentoversiktForIdent(
        ident: String,
        tema: Tema,
    ): Dokumentoversikt = safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema = tema)

    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
    ): ByteArray = safSelvbetjeningClient.hentDokument(journalpostId, dokumentInfoId)
}
