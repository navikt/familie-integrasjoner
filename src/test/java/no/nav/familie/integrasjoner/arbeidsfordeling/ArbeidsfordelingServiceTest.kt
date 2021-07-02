package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.kontrakter.felles.Tema
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ArbeidsfordelingServiceTest {

    private val restClient: ArbeidsfordelingRestClient = mockk()
    private val pdlRestClient: PdlRestClient = mockk()
    private val arbeidsfordelingService =
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

        assertThrows<IllegalStateException> { arbeidsfordelingService.finnLokaltNavKontor(ident, Tema.ENF) }
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

        every { restClient.hentEnhet(any()) } returns mockk()

        arbeidsfordelingService.finnLokaltNavKontor(ident, Tema.ENF)

        verify { restClient.hentEnhet("2372") }
    }


}