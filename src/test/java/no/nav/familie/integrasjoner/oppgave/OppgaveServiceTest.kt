package no.nav.familie.integrasjoner.oppgave

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.saksbehandler.SaksbehandlerService
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class OppgaveServiceTest {

    val oppgaveRestClient = mockk<OppgaveRestClient>()
    val aktørService = mockk<AktørService>()
    val saksbehandlerService = mockk<SaksbehandlerService>()

    val oppgaveService = OppgaveService(oppgaveRestClient, aktørService, saksbehandlerService)

    val mapper = listOf(
        MappeDto(
            id = 1,
            navn = "132",
            enhetsnr = "4483"
        ),
        MappeDto(
            id = 2,
            navn = "123",
            enhetsnr = "4483",
            tema = "PEN"
        )
    )
    val expectedResponse = FinnMappeResponseDto(antallTreffTotalt = 2, mapper = mapper)

    @Test
    fun `Skal filtrere bort mapper med tema`() {
        every { oppgaveRestClient.finnMapper(any()) } returns expectedResponse
        val finnMapper = oppgaveService.finnMapper("4483")
        assertThat(finnMapper.size).isEqualTo(1)
        assertThat(finnMapper.first().id).isEqualTo(1)
    }

    @Test
    fun `Skal oppdatere antall treff i FinnMappeResponseDto`() {
        every { oppgaveRestClient.finnMapper(any()) } returns expectedResponse
        val finnMappeRequest = FinnMappeRequest(
            tema = listOf(),
            enhetsnr = "4483",
            opprettetFom = null,
            limit = 1000
        )
        val response = oppgaveService.finnMapper(finnMappeRequest)
        assertThat(response.antallTreffTotalt).isEqualTo(1)
    }
}
