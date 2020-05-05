package no.nav.familie.integrasjoner.dokarkiv.client.domene

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OppdaterJournalpostRequest(val avsenderMottaker: AvsenderMottaker? = null,
                                      val bruker: Bruker? = null,
                                      val tema: String? = null,
                                      val behandlingstema: String? = null,
                                      val tittel: String? = null,
                                      val journalfoerendeEnhet: String? = null,
                                      val sak: Sak? = null,
                                      val dokumenter: List<DokumentInfo>? = null)
