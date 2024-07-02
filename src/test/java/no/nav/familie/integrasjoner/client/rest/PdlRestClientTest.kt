package no.nav.familie.integrasjoner.client.rest

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.integrasjoner.geografisktilknytning.PdlHentGeografiskTilknytning
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PdlRestClientTest {
    @Test
    fun `skal parse harUnathorizedFeil`() {
        val response: PdlResponse<PdlHentGeografiskTilknytning> = objectMapper.readValue<PdlResponse<PdlHentGeografiskTilknytning>>(jsonFeilmelding)

        assertThat(response.harUnauthorizedFeil()).isTrue()
        val detaljertFeilmeldingFraPdl = response.errors?.joinToString { it.extensions.toString() }
        assertThat(detaljertFeilmeldingFraPdl).contains("adressebeskyttelse_strengt_fortrolig_adresse")
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
