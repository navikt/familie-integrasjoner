package no.nav.familie.integrasjoner.oppgave.domene

import no.nav.familie.kontrakter.felles.oppgave.Oppgave

data class FinnOppgaveResponseDto(val antallTreffTotalt: Long,
                                  val oppgaver: List<Oppgave>)