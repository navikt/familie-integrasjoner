package no.nav.familie.integrasjoner.personopplysning.internal

import io.mockk.mockk
import no.nav.familie.kontrakter.felles.jsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PdlResponseTest {
    @Nested
    inner class TilPdlUnauthorizedDetails {
        @Test
        fun `skal hente ut PdlUnauthorizedDetails fra PdlResponse`() {
            // Arrange
            // Details blir deserialisert til `LinkedHashMap` når verdien er Any.
            val unauthorizedDetailsSomLinkedHashMap: Any =
                linkedMapOf(
                    "type" to "pdl-tilgangsstyring",
                    "cause" to "Bruker mangler rollen '0000-GA-Strengt_Fortrolig_Adresse'",
                    "policy" to "adressebeskyttelse_strengt_fortrolig_adresse",
                )

            val pdlResponse =
                PdlResponse<Any>(
                    data = mockk(),
                    errors =
                        listOf(
                            PdlError(
                                message = "Ikke tilgang til å se person",
                                extensions =
                                    PdlErrorExtensions(
                                        code = "unauthorized",
                                        details = unauthorizedDetailsSomLinkedHashMap,
                                    ),
                            ),
                        ),
                    extensions = null,
                )

            // Act
            val pdlUnauthorizedDetails: List<PdlUnauthorizedDetails> = pdlResponse.tilPdlUnauthorizedDetails()

            // Assert
            assertNotNull(pdlUnauthorizedDetails)
            assertThat(pdlUnauthorizedDetails).isEqualTo(listOf(jsonMapper.convertValue(unauthorizedDetailsSomLinkedHashMap, PdlUnauthorizedDetails::class.java)))
        }
    }
}
