package no.nav.familie.integrasjoner.client.rest

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.geografisktilknytning.PdlHentGeografiskTilknytning
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import tools.jackson.module.kotlin.readValue
import java.net.URI

class PdlRestClientTest {
    private val restOperations: RestOperations = mockk()
    private val pdlRestClient: PdlRestClient = PdlRestClient(pdlBaseUrl = URI.create("pdl"), restTemplate = restOperations)

    @Test
    fun `skal parse harUnathorizedFeil`() {
        val response: PdlResponse<PdlHentGeografiskTilknytning> = jsonMapper.readValue<PdlResponse<PdlHentGeografiskTilknytning>>(jsonFeilmelding)

        assertThat(response.harUnauthorizedFeil()).isTrue()
        val detaljertFeilmeldingFraPdl = response.errors?.joinToString { it.extensions.toString() }
        assertThat(detaljertFeilmeldingFraPdl).contains("adressebeskyttelse_strengt_fortrolig_adresse")
    }

    @Nested
    inner class HentAdressebeskyttelse {
        @Test
        fun `skal kaste OppslagException hvis kall mot PDL feiler`() {
            // Arrange
            every { restOperations.exchange<PdlResponse<PdlPersonMedAdressebeskyttelse>>(any<URI>(), eq(HttpMethod.POST), any<HttpEntity<PdlPersonRequest>>()) } throws RuntimeException("Noe gikk galt")

            // Act & Assert
            val oppslagException =
                assertThrows<OppslagException> {
                    pdlRestClient.hentAdressebeskyttelse("12345678910", Tema.BAR)
                }

            assertThat(oppslagException.message).isEqualTo("Feil ved henting av adressebeskyttelse")
            assertThat(oppslagException.kilde).isEqualTo("PdlRestClient.hentAdressebeskyttelse")
            assertThat(oppslagException.level).isEqualTo(OppslagException.Level.MEDIUM)
            assertThat(oppslagException.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Hentet fra PDL dokumentasjonen
     */
    val jsonFeilmelding =
        """
        {
          "errors": [
            {
              "message": "Ikke tilgang til å se person",
              "locations": [
                {
                  "line": 2,
                  "column": 5
                }
              ],
              "path": [
                "hentPerson"
              ],
              "extensions": {
                "code": "unauthorized",
                "details": {
                  "type": "abac-deny",
                  "cause": "cause-0001-manglerrolle",
                  "policy": "adressebeskyttelse_strengt_fortrolig_adresse"
                },
                "classification": "ExecutionAborted"
              }
            }
          ],
          "data": {
            "hentPerson": null
          }
        }

        """.trimIndent()
}
