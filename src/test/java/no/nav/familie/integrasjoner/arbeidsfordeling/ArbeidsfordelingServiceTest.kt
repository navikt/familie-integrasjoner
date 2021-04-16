package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalStateException

internal class ArbeidsfordelingServiceTest {

    val restClient: ArbeidsfordelingRestClient = mockk()
    val pdlRestClient: PdlRestClient = mockk()
    val arbeidsfordelingService =
            ArbeidsfordelingService(klient = mockk(),
                                    restClient = restClient,
                                    pdlRestClient = pdlRestClient,
                                    personopplysningerService = mockk())

    val ident = "12345678"

    @Test
    fun `skal kaste feil ved geografisk tilknytning ikke definert`() {

        every {
            pdlRestClient.hentGeografiskTilknytning(ident,
                                                    any())
        } returns GeografiskTilknytningDto(GeografiskTilknytningType.UDEFINERT, null, null, null)

        assertThrows<IllegalStateException> { arbeidsfordelingService.finnLokaltNavKontor(ident, "ENF") }
    }

    @Test
    fun `skal utlede riktig geografisk tilknytning kode`() {

        every {
            pdlRestClient.hentGeografiskTilknytning(ident,
                                                    any())
        } returns GeografiskTilknytningDto(gtType = GeografiskTilknytningType.KOMMUNE,
                                           gtKommune = "2372",
                                           gtBydel = null,
                                           gtLand = null)

        every { restClient.hentEnhet(any())} returns mockk()

        arbeidsfordelingService.finnLokaltNavKontor(ident, "ENF")

        verify { restClient.hentEnhet("2372") }
    }


}