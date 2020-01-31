package no.nav.familie.integrasjoner.dokarkiv.api

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty


data class ArkiverDokumentRequest(@field:NotBlank val fnr: String,
                                  val forsøkFerdigstill: Boolean,
                                  @field:NotEmpty val dokumenter: List<Dokument>,
                                  val fagsakId: String?= null,
                                  val journalførendeEnhet: String? = null) //Skal ikke settes for innkommende hvis Ruting gjøres av BRUT001
