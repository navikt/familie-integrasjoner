package no.nav.familie.integrasjoner.oppgave.domene

data class FinnOppgaveResponseDto(val antallTreffTotalt: Long,
                                  val oppgaver: List<OppgaveJsonDto>)