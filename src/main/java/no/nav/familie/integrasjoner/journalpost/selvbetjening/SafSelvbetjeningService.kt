package no.nav.familie.integrasjoner.journalpost.selvbetjening

import no.nav.familie.integrasjoner.journalpost.graphql.enums.Tema
import no.nav.familie.integrasjoner.journalpost.graphql.hentdokumentoversikt.Dokumentoversikt
import org.springframework.stereotype.Service

@Service
class SafSelvbetjeningService(
    private val safSelvbetjeningClient: SafSelvbetjeningClient,
) {
    suspend fun hentDokumentoversiktForIdent(
        ident: String,
        tema: Tema,
    ): Dokumentoversikt = safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema = tema)
}
