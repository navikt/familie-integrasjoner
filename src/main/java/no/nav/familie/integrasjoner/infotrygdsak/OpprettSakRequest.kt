package no.nav.familie.integrasjoner.infotrygdsak

import java.time.LocalDate

data class OpprettSakRequest(val fnr: String,
                             val stonadsklassifisering1: String?,
                             val stonadsklassifisering2: String?,
                             val mottattDato: LocalDate?,
                             val regNavEnhetId: String?,
                             val regNavBrukerId: String?,
                             val behNavEnhetId: String?,
                             val bekreftelsesbrev: Boolean)
