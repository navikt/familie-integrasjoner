package no.nav.familie.integrasjoner.oppgave

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
class OppgaveRestClientTestConfig {

    @Bean
    @Profile("mock-oppgave")
    @Primary
    fun oppgaveMockRestClient(): OppgaveRestClient {
        val klient: OppgaveRestClient = mockk(relaxed = false)
        val response = Oppgave(
            id = 42,
            aktoerId = "1234",
            identer = listOf(OppgaveIdentV2("11111111111", IdentGruppe.FOLKEREGISTERIDENT)),
            journalpostId = "1234",
            tildeltEnhetsnr = "4820",
            tilordnetRessurs = "test@nav.no",
            behandlesAvApplikasjon = "FS22",
            beskrivelse = "Beskrivelse for oppgave",
            tema = Tema.BAR,
            oppgavetype = Oppgavetype.Journalføring.value,
            opprettetTidspunkt = LocalDate.of(
                2020,
                1,
                1
            ).toString(),
            fristFerdigstillelse = LocalDate.of(
                2020,
                2,
                1
            ).toString(),
            prioritet = OppgavePrioritet.NORM,
            status = StatusEnum.OPPRETTET
        )

        every {
            klient.finnOppgaver(any())
        } returns FinnOppgaveResponseDto(1, listOf(response))

        every {
            klient.finnÅpenBehandleSakOppgave(any())
        } returns response

        every {
            klient.finnOppgaveMedId(any())
        } returns response

        every {
            klient.opprettOppgave(any())
        } returns 42

        every { klient.oppdaterOppgave(any()) } returns response

        every { klient.ping() } returns Unit

        return klient
    }
}
