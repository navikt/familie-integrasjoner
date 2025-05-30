package no.nav.familie.integrasjoner.journalpost.selvbetjening

import no.nav.familie.integrasjoner.journalpost.graphql.hentjournalposter.Dokumentoversikt
import org.springframework.stereotype.Service

@Service
class SafSelvbetjeningService(
    private val safSelvbetjeningClient: SafSelvbetjeningClient,
) {
    suspend fun hentDokumentoversiktForIdent(ident: String): Dokumentoversikt = safSelvbetjeningClient.hentDokumentoversiktForIdent(ident)
}
