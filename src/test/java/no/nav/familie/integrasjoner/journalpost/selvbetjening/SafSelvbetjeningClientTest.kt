package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.familie.integrasjoner.safselvbetjening.generated.HentDokumentoversikt
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestOperations
import java.net.URI

class SafSelvbetjeningClientTest {
    private val safSelvbetjeningGraphQLClient: GraphQLWebClient = mockk()
    private val restTemplate: RestOperations = mockk()
    private val safSelvbetjeningClient: SafSelvbetjeningClient = SafSelvbetjeningClient(safSelvbetjeningGraphQLClient, restTemplate, URI.create("https://test.no"))

    @Nested
    inner class HentDokumentoversiktForIdent {
        @Test
        fun `skal returnere dokumentoversikt for ident og tema når respons ikke inneholder feil og data ikke er null`() {
            // Arrange
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns mockk()
                    every { errors } returns null
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            // Act
            val dokumentoversiktResultat = runBlocking { safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema) }

            // Assert
            assertThat(dokumentoversiktResultat).isNotNull
        }

        @Test
        fun `skal kaste exception dersom respons inneholder feil`() {
            // Arrange
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns null
                    every { errors } returns listOf(mockk { every { message } returns "Feilmelding" })
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            // Act & Assert
            val exception =
                assertThrows<SafSelvbetjeningException> {
                    runBlocking { safSelvbetjeningClient.hentDokumentoversiktForIdent(ident, tema) }
                }

            assertThat(exception.message).isEqualTo("Feil ved henting av dokumentoversikt for bruker: Feilmelding")
        }

        @Test
        fun `skal kaste feil dersom respons ikke inneholder feil men data er null`() {
            // Arrange
            val ident = "12345678901"
            val tema = Tema.BAR
            val respons =
                mockk<GraphQLClientResponse<HentDokumentoversikt.Result>> {
                    every { data } returns null
                    every { errors } returns null
                }

            coEvery { safSelvbetjeningGraphQLClient.execute(any<HentDokumentoversikt>()) } returns respons

            // Act & Assert
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
        fun `skal returnere dokument dersom henting er vellykket`() {
            // Arrange
            val journalpostId = "123"
            val dokumentInfoId = "456"
            val expectedBytes = byteArrayOf(1, 2, 3)
            val uri = URI.create("https://test.no/rest/hentdokument/123/456/ARKIV")

            val byteArrayRef = object : ParameterizedTypeReference<ByteArray>() {}

            every {
                restTemplate.exchange(
                    uri,
                    eq(HttpMethod.GET),
                    match<HttpEntity<*>> {
                        it.headers.getFirst(HttpHeaders.ACCEPT) == "application/pdf"
                    },
                    eq(byteArrayRef),
                )
            } returns
                mockk {
                    every { statusCode } returns HttpStatus.OK
                    every { body } returns expectedBytes
                }

            // Act
            val result = safSelvbetjeningClient.hentDokument(journalpostId, dokumentInfoId)

            // Assert
            assertThat(result).isEqualTo(expectedBytes)
        }

        @Test
        fun `skal kaste feil dersom henting av dokument feiler`() {
            // Arrange
            val journalpostId = "123"
            val dokumentInfoId = "456"
            val uri = URI.create("https://test.no/rest/hentdokument/123/456/ARKIV")

            val byteArrayRef = object : ParameterizedTypeReference<ByteArray>() {}

            every {
                restTemplate.exchange(
                    uri,
                    eq(HttpMethod.GET),
                    match<HttpEntity<*>> {
                        it.headers.getFirst(HttpHeaders.ACCEPT) == "application/pdf"
                    },
                    eq(byteArrayRef),
                )
            } throws RuntimeException("Error")

            // Act & Assert
            val exception =
                assertThrows<SafSelvbetjeningException> {
                    safSelvbetjeningClient.hentDokument(journalpostId, dokumentInfoId)
                }

            assertThat(exception.message).isEqualTo("Ukjent feil ved henting av dokument. Feil ved kall mot uri=$uri")
        }
    }
}
