package no.nav.familie.integrasjoner.axsys

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.AxsysRestClient
import no.nav.familie.kontrakter.felles.NavIdent
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.enhet.Enhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AxsysServiceTest {
    private val axsysRestClient: AxsysRestClient = mockk()
    private val axsysService: AxsysService = AxsysService(axsysRestClient = axsysRestClient)

    @Test
    fun `Skal hente enheter nav-identen har tilgang til for gitt tema`() {
        // Arrange
        val navIdent = NavIdent("Z12345")
        val enhet1 = "1234"
        val enhet2 = "5678"

        every {
            axsysRestClient.hentEnheterNavIdentHarTilgangTil(navIdent)
        } returns
            TilgangV2DTO(
                listOf(
                    EnhetV2DTO(enhetId = enhet1, temaer = listOf(Tema.BAR.name), navn = "Enhetsnavn1"),
                    EnhetV2DTO(enhetId = enhet2, temaer = listOf(Tema.KON.name), navn = "Enhetsnavn2"),
                ),
            )

        // Act
        val enheter = axsysService.hentEnheterNavIdentHarTilgangTil(navIdent = navIdent, tema = Tema.BAR)

        // Assert
        assertThat(enheter).containsOnly(Enhet("1234", "Enhetsnavn1"))
    }
}
