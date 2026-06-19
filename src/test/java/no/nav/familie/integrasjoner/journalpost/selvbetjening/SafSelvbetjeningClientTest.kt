package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.safselvbetjening.generated.HentDokumentoversikt
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.RestClient
import java.net.URI

class SafSelvbetjeningClientTest {
    private val safSelvbetjeningGraphQLClient: GraphQLWebClient = mockk()
    private val restClient: RestClient = mockk(relaxed = true)
    private val safSelvbetjeningClient: SafSelvbetjeningClient =
        SafSelvbetjeningClient(
            safSelvbetjeningGraphQLClient = safSelvbetjeningGraphQLClient,
            safSelvbetjeningURI = URI.create("https://test.no"),
            restClient = restClient,
        )

    @Nested
    inner class HentDokumentoversiktForIdent {
        @Test
        fun `skal returnere dokumentoversikt for ident og tema når respons ikke inneholder feil og data ikke er null`() {
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns mockk()
                    every { errors } returns null
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            val dokumentoversiktResultat = runBlocking { safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema) }

            assertThat(dokumentoversiktResultat).isNotNull
        }

        @Test
        fun `skal kaste exception dersom respons inneholder feil`() {
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns null
                    every { errors } returns listOf(mockk { every { message } returns "Feilmelding" })
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            val exception =
                assertThrows<SafSelvbetjeningException> {
                    runBlocking { safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema) }
                }

            assertThat(exception.message).isEqualTo("Feil ved henting av dokumentoversikt for bruker: Feilmelding")
        }

        @Test
        fun `skal kaste feil dersom respons ikke inneholder feil men data er null`() {
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns null
                    every { errors } returns null
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            val exception =
                assertThrows<SafSelvbetjeningException> {
                    runBlocking { safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema) }
                }

            assertThat(exception.message).isEqualTo("Ingen data mottatt fra SAF for ident")
        }
    }

    @Nested
    inner class HentDokument {
        @Test
        fun `skal kaste feil dersom henting av dokument feiler`() {
            val responseSpec: RestClient.ResponseSpec = mockk()

            every { responseSpec.body(any<Class<*>>()) } throws RuntimeException("Error")

            val exception =
                assertThrows<SafSelvbetjeningException> {
                    safSelvbetjeningClient.hentDokument("123", "456")
                }

            assertThat(exception.message).contains("Ukjent feil ved henting av dokument")
        }
    }
}
