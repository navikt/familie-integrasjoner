package no.nav.familie.integrasjoner.infotrygdsak

import java.time.LocalDate

class OpprettInfotrygdSakRequest(var fnr: String,
                                 var fagomrade: String,
                                 var stonadsklassifisering_2: String? = null,
                                 var stonadsklassifisering_3: String? = null,
                                 var type: String? = null,
                                 var opprettetAv: String? = null,
                                 var opprettetAvOrganisasjonsEnhetsId: String? = null,
                                 var mottakerOrganisasjonsEnhetsId: String? = null,
                                 var motattdato: LocalDate? = null,
                                 var sendBekreftelsesbrev: Boolean? = null,
                                 var oppgaveId: String? = null,
                                 var oppgaveOrganisasjonsenhetId: String? = null)
