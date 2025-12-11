package no.nav.familie.integrasjoner.oppgave

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.saksbehandler.SaksbehandlerService
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext.SYSTEM_FORKORTELSE
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import no.nav.familie.kontrakter.felles.saksbehandler.Saksbehandler
import no.nav.familie.log.mdc.MDCConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.time.LocalDate
import java.util.UUID

internal class OppgaveServiceTest {
    val oppgaveRestClient = mockk<OppgaveRestClient>()
    val saksbehandlerService = mockk<SaksbehandlerService>()

    val oppgaveService = OppgaveService(oppgaveRestClient, saksbehandlerService)

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

    @AfterEach
    internal fun tearDown() {
        unmockkObject(SikkerhetsContext)
    }

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

    @Nested
    inner class TilordneEnhetOgNullstillTilordnetRessursTest {
        @Test
        fun `skal fjerne mappe fra oppgave dersom det settes til true`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            val oppgaveId = 1L
            val oppgaveByttEnhetOgTilordnetRessursRessursSlot = slot<OppgaveByttEnhetOgTilordnetRessurs>()
            val oppgaveMedMappe = Oppgave(id = oppgaveId, mappeId = 50)

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns oppgaveMedMappe
            every { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(capture(oppgaveByttEnhetOgTilordnetRessursRessursSlot)) } returns oppgaveMedMappe

            // Act
            oppgaveService.tilordneEnhetOgNullstillTilordnetRessurs(
                oppgaveId = 1L,
                enhet = "nyEnhet",
                fjernMappeFraOppgave = true,
                nullstillTilordnetRessurs = false,
                versjon = 0,
                nyMappeId = null,
            )

            // Assert
            val oppgaveByttEnhetOgTilordnetRessurs = oppgaveByttEnhetOgTilordnetRessursRessursSlot.captured

            assertThat(oppgaveByttEnhetOgTilordnetRessurs.id).isEqualTo(1)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.mappeId).isNull()
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.tildeltEnhetsnr).isEqualTo("nyEnhet")

            verify(exactly = 1) { oppgaveRestClient.finnOppgaveMedId(oppgaveId) }
            verify(exactly = 1) { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(oppgaveByttEnhetOgTilordnetRessurs) }
        }

        @Test
        fun `skal ikke fjerne mappe fra oppgave dersom det settes til false`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            val oppgaveId = 1L
            val oppgaveByttEnhetOgTilordnetRessursRessursSlot = slot<OppgaveByttEnhetOgTilordnetRessurs>()
            val oppgaveMedMappe = Oppgave(id = oppgaveId, mappeId = 50)

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns oppgaveMedMappe
            every { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(capture(oppgaveByttEnhetOgTilordnetRessursRessursSlot)) } returns oppgaveMedMappe

            // Act
            oppgaveService.tilordneEnhetOgNullstillTilordnetRessurs(
                oppgaveId = 1L,
                enhet = "nyEnhet",
                fjernMappeFraOppgave = false,
                nullstillTilordnetRessurs = false,
                versjon = 0,
                nyMappeId = null,
            )

            // Assert
            val oppgaveByttEnhetOgTilordnetRessurs = oppgaveByttEnhetOgTilordnetRessursRessursSlot.captured

            assertThat(oppgaveByttEnhetOgTilordnetRessurs.id).isEqualTo(1)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.mappeId).isEqualTo(50)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.tildeltEnhetsnr).isEqualTo("nyEnhet")

            verify(exactly = 1) { oppgaveRestClient.finnOppgaveMedId(oppgaveId) }
            verify(exactly = 1) { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(oppgaveByttEnhetOgTilordnetRessurs) }
        }

        @Test
        fun `skal nullstille tilordnet ressurs hvis det settes til true`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            val oppgaveId = 1L
            val oppgaveByttEnhetOgTilordnetRessursRessursSlot = slot<OppgaveByttEnhetOgTilordnetRessurs>()
            val oppgaveMedMappe = Oppgave(id = oppgaveId, mappeId = 50, tilordnetRessurs = "tilordnetRessurs")

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns oppgaveMedMappe
            every { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(capture(oppgaveByttEnhetOgTilordnetRessursRessursSlot)) } returns oppgaveMedMappe

            // Act
            oppgaveService.tilordneEnhetOgNullstillTilordnetRessurs(
                oppgaveId = 1L,
                enhet = "nyEnhet",
                fjernMappeFraOppgave = false,
                nullstillTilordnetRessurs = true,
                versjon = 0,
                nyMappeId = null,
            )

            // Assert
            val oppgaveByttEnhetOgTilordnetRessurs = oppgaveByttEnhetOgTilordnetRessursRessursSlot.captured

            assertThat(oppgaveByttEnhetOgTilordnetRessurs.id).isEqualTo(1L)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.tilordnetRessurs).isNull()

            verify(exactly = 1) { oppgaveRestClient.finnOppgaveMedId(oppgaveId) }
            verify(exactly = 1) { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(oppgaveByttEnhetOgTilordnetRessurs) }
        }

        @Test
        fun `skal flytte oppgave og sette ny mappeId hvis det er sendt inn`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"
            val oppgaveId = 1L
            val oppgaveByttEnhetOgTilordnetRessursRessursSlot = slot<OppgaveByttEnhetOgTilordnetRessurs>()
            val oppgaveMedMappe = Oppgave(id = oppgaveId, mappeId = 50, tilordnetRessurs = "tilordnetRessurs")

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )

            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns oppgaveMedMappe
            every { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(capture(oppgaveByttEnhetOgTilordnetRessursRessursSlot)) } returns oppgaveMedMappe

            // Act
            oppgaveService.tilordneEnhetOgNullstillTilordnetRessurs(
                oppgaveId = 1L,
                enhet = "nyEnhet",
                fjernMappeFraOppgave = false,
                nullstillTilordnetRessurs = true,
                versjon = 0,
                nyMappeId = 4500,
            )

            // Assert
            val oppgaveByttEnhetOgTilordnetRessurs = oppgaveByttEnhetOgTilordnetRessursRessursSlot.captured

            assertThat(oppgaveByttEnhetOgTilordnetRessurs.id).isEqualTo(1L)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.tilordnetRessurs).isNull()
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.mappeId).isEqualTo(4500)

            verify(exactly = 1) { oppgaveRestClient.finnOppgaveMedId(oppgaveId) }
            verify(exactly = 1) { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(oppgaveByttEnhetOgTilordnetRessurs) }
        }

        @Test
        fun `skal sette endretAvEnhetsnr til saksbehandler sin enhet hvis det er SB som gjør endringen`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            val oppgaveId = 1L
            val oppgaveByttEnhetOgTilordnetRessursRessursSlot = slot<OppgaveByttEnhetOgTilordnetRessurs>()
            val oppgaveMedMappe = Oppgave(id = oppgaveId, mappeId = 50, tilordnetRessurs = "tilordnetRessurs")

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns oppgaveMedMappe
            every { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(capture(oppgaveByttEnhetOgTilordnetRessursRessursSlot)) } returns oppgaveMedMappe

            // Act
            oppgaveService.tilordneEnhetOgNullstillTilordnetRessurs(
                oppgaveId = 1L,
                enhet = "nyEnhet",
                fjernMappeFraOppgave = false,
                nullstillTilordnetRessurs = true,
                versjon = 0,
                nyMappeId = 4500,
            )

            // Assert
            val oppgaveByttEnhetOgTilordnetRessurs = oppgaveByttEnhetOgTilordnetRessursRessursSlot.captured

            assertThat(oppgaveByttEnhetOgTilordnetRessurs.id).isEqualTo(1L)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.tilordnetRessurs).isNull()
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.mappeId).isEqualTo(4500)
            assertThat(oppgaveByttEnhetOgTilordnetRessurs.endretAvEnhetsnr).isEqualTo("4321")

            verify(exactly = 1) { oppgaveRestClient.finnOppgaveMedId(oppgaveId) }
            verify(exactly = 1) { oppgaveRestClient.oppdaterEnhetOgTilordnetRessurs(oppgaveByttEnhetOgTilordnetRessurs) }
        }
    }

    @Nested
    inner class TilbakestillFordelingPåOppgaveTest {
        @Test
        fun `Skal sette endretAvEnhetsnr til SB sin enhet hvis det er SB som har gjort endringen`() {
            // Arrange
            mockkObject(SikkerhetsContext)
            val oppgaveId = 3L
            val originalOppgave =
                Oppgave(
                    id = oppgaveId,
                    versjon = 5,
                    endretAvEnhetsnr = "0000",
                    status = StatusEnum.UNDER_BEHANDLING,
                )
            val oppgaveSlot = slot<Oppgave>()

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z444444"

            every { saksbehandlerService.hentSaksbehandler("Z444444") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )

            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns originalOppgave
            every { oppgaveRestClient.oppdaterOppgave(capture(oppgaveSlot)) } returns originalOppgave

            // Act
            oppgaveService.tilbakestillFordelingPåOppgave(
                oppgaveId = oppgaveId,
                versjon = 5,
            )

            // Assert
            assertThat(oppgaveSlot.captured.endretAvEnhetsnr).isEqualTo("4321")
        }

        @Test
        fun `tilbakestillFordeling skal bruke eksisterende endretAvEnhetsnr for systembruker`() {
            // Arrange
            mockkObject(SikkerhetsContext)
            val oppgaveId = 4L
            val originalOppgave =
                Oppgave(
                    id = oppgaveId,
                    versjon = 2,
                    endretAvEnhetsnr = "2000",
                    status = StatusEnum.UNDER_BEHANDLING,
                )
            val oppgaveSlot = slot<Oppgave>()

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns SYSTEM_FORKORTELSE
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns originalOppgave
            every { oppgaveRestClient.oppdaterOppgave(capture(oppgaveSlot)) } returns originalOppgave

            // Act
            oppgaveService.tilbakestillFordelingPåOppgave(
                oppgaveId = oppgaveId,
                versjon = 2,
            )

            // Assert
            assertThat(oppgaveSlot.captured.endretAvEnhetsnr).isEqualTo("2000")
        }
    }

    @Nested
    inner class FerdigstillTest {
        @Test
        fun `ferdigstill skal bruke eksisterende endretAvEnhetsnr på oppgave for systembruker`() {
            // Arrange
            mockkObject(SikkerhetsContext)
            val oppgaveId = 6L

            val originalOppgave =
                Oppgave(
                    id = oppgaveId,
                    versjon = 4,
                    endretAvEnhetsnr = "2000",
                    status = StatusEnum.OPPRETTET,
                )

            val oppgaveSlot = slot<Oppgave>()

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns SYSTEM_FORKORTELSE
            every { saksbehandlerService.hentSaksbehandler(any()) } throws AssertionError("Skal ikke kalles")
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns originalOppgave
            every { oppgaveRestClient.oppdaterOppgave(capture(oppgaveSlot)) } returns originalOppgave

            // Act
            oppgaveService.ferdigstill(
                oppgaveId = oppgaveId,
                versjon = 4,
            )

            // Assert
            assertThat(oppgaveSlot.captured.endretAvEnhetsnr).isEqualTo("2000")
            assertThat(oppgaveSlot.captured.status).isEqualTo(StatusEnum.FERDIGSTILT)
        }

        @Test
        fun `ferdigstill skal sette endretAvEnhetsnr til saksbehandler sin enhet hvis det er SB som har gjort endringen`() {
            // Arrange
            mockkObject(SikkerhetsContext)

            val oppgaveId = 5L
            val originalOppgave =
                Oppgave(
                    id = oppgaveId,
                    versjon = 7,
                    endretAvEnhetsnr = "4812",
                    status = StatusEnum.UNDER_BEHANDLING,
                )
            val oppgaveSlot = slot<Oppgave>()

            every { SikkerhetsContext.hentSaksbehandlerEllerSystembruker() } returns "Z555555"
            every { oppgaveRestClient.finnOppgaveMedId(oppgaveId) } returns originalOppgave
            every { saksbehandlerService.hentSaksbehandler("Z555555") } returns
                Saksbehandler(
                    enhet = "4321",
                    azureId = UUID.randomUUID(),
                    navIdent = "testIdent",
                    fornavn = "testNavn",
                    etternavn = "testEtternavn",
                )

            every { oppgaveRestClient.oppdaterOppgave(capture(oppgaveSlot)) } returns originalOppgave

            // Act
            oppgaveService.ferdigstill(
                oppgaveId = oppgaveId,
                versjon = 7,
            )

            // Assert
            assertThat(oppgaveSlot.captured.endretAvEnhetsnr).isEqualTo("4321")
            assertThat(oppgaveSlot.captured.status).isEqualTo(StatusEnum.FERDIGSTILT)
        }
    }
}
