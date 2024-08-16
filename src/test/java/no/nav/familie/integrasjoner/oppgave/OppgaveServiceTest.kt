package no.nav.familie.integrasjoner.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.saksbehandler.SaksbehandlerService
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {
    val oppgaveRestClient = mockk<OppgaveRestClient>()
    val aktørService = mockk<AktørService>()
    val saksbehandlerService = mockk<SaksbehandlerService>()

    val oppgaveService = OppgaveService(oppgaveRestClient, aktørService, saksbehandlerService)

    val mapper =
        listOf(
            MappeDto(
                id = 1,
                navn = "132",
                enhetsnr = "4483",
            ),
            MappeDto(
                id = 2,
                navn = "123",
                enhetsnr = "4483",
                tema = "PEN",
            ),
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
        val finnMappeRequest =
            FinnMappeRequest(
                tema = listOf(),
                enhetsnr = "4483",
                opprettetFom = null,
                limit = 1000,
            )
        val response = oppgaveService.finnMapper(finnMappeRequest)
        assertThat(response.antallTreffTotalt).isEqualTo(1)
    }

    @Test
    fun `skal sette personident når identgruppe er folkeregisterident`() {
        val personIdent = "12345678912"
        val oppgaveSlot = slot<Oppgave>()
        val request =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(personIdent, IdentGruppe.FOLKEREGISTERIDENT),
                fristFerdigstillelse = LocalDate.now().plusDays(3),
                behandlingstema = "behandlingstema",
                enhetsnummer = "enhetsnummer",
                tema = Tema.BAR,
                oppgavetype = Oppgavetype.BehandleSak,
                saksId = "saksid",
                beskrivelse = "Oppgavetekst",
            )

        every { oppgaveRestClient.opprettOppgave(capture(oppgaveSlot)) } returns 0L
        every { saksbehandlerService.hentNavIdent(any()) } returns "ident"

        val result = oppgaveService.opprettOppgave(request)

        assertThat(oppgaveSlot.captured.personident).isEqualTo(personIdent)
        assertThat(oppgaveSlot.captured.aktoerId).isNull()
    }

    @Test
    fun `skal sette aktørid når identgruppe er aktørid`() {
        val aktørid = "12345678912"
        val oppgaveSlot = slot<Oppgave>()
        val request =
            OpprettOppgaveRequest(
                ident = OppgaveIdentV2(aktørid, IdentGruppe.AKTOERID),
                fristFerdigstillelse = LocalDate.now().plusDays(3),
                behandlingstema = "behandlingstema",
                enhetsnummer = "enhetsnummer",
                tema = Tema.BAR,
                oppgavetype = Oppgavetype.BehandleSak,
                saksId = "saksid",
                beskrivelse = "Oppgavetekst",
            )

        every { oppgaveRestClient.opprettOppgave(capture(oppgaveSlot)) } returns 0L
        every { saksbehandlerService.hentNavIdent(any()) } returns "ident"

        val result = oppgaveService.opprettOppgave(request)

        assertThat(oppgaveSlot.captured.aktoerId).isEqualTo(aktørid)
        assertThat(oppgaveSlot.captured.personident).isNull()
    }
}
