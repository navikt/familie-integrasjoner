package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.integrasjoner.journalpost.graphql.HentDokumentoversikt
import no.nav.familie.integrasjoner.journalpost.graphql.enums.Tema
import no.nav.familie.integrasjoner.journalpost.graphql.hentdokumentoversikt.Dokumentoversikt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class SafSelvbetjeningClient(
    @Qualifier("SafSelvbetjening") private val graphQLWebClient: GraphQLWebClient,
) {
    fun hentDokumentoversiktForIdent(
        ident: String,
        tema: Tema,
    ): Dokumentoversikt =
        runBlocking(Dispatchers.Default) {
            val response =
                graphQLWebClient.execute(
                    HentDokumentoversikt(variables = HentDokumentoversikt.Variables(ident = ident, tema = tema)),
                )
            if (response.errors.isNullOrEmpty()) {
                response.data!!.dokumentoversiktSelvbetjening
            } else {
                throw OppslagException(
                    "Feil ved henting av dokumentoversikt for bruker: ${
                        response.errors!!.joinToString(", ") { it.message }
                    }",
                    "saf.selvbetjening.hentDokumentoversiktForIdent",
                    Level.MEDIUM,
                )
            }
        }
}
