package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.familie.integrasjoner.journalpost.graphql.HentJournalposter
import no.nav.familie.integrasjoner.journalpost.graphql.hentjournalposter.Dokumentoversikt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class SafSelvbetjeningClient(
    @Qualifier("SafSelvbetjening") val graphQLWebClient: GraphQLWebClient,
) {
    suspend fun hentDokumentoversiktForIdent(ident: String): Dokumentoversikt {
        val response = graphQLWebClient.execute(HentJournalposter(variables = HentJournalposter.Variables(ident = ident)))
        if (response.errors.isNullOrEmpty()) {
            return response.data!!.dokumentoversiktSelvbetjening
        } else {
            throw IllegalStateException("Feil ved henting av dokumentoversikt for bruker: ${response.errors!!.joinToString(", ") { it.message }}")
        }
    }
}
