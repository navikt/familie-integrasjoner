package no.nav.familie.integrasjoner.dokarkiv.client.domene

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.familie.kontrakter.felles.dokarkiv.AvsenderMottaker
import no.nav.familie.kontrakter.felles.dokarkiv.DokarkivBruker
import no.nav.familie.kontrakter.felles.dokarkiv.Sak

@JsonInclude(JsonInclude.Include.NON_NULL)
class OpprettJournalpostRequest(
    val journalpostType: JournalpostType? = null,
    val avsenderMottaker: AvsenderMottaker? = null,
    val bruker: DokarkivBruker? = null,
    val tema: String? = null,
    val behandlingstema: String? = null,
    val tittel: String? = null,
    val kanal: String? = null,
    val journalfoerendeEnhet: String? = null,
    val eksternReferanseId: String? = null,
    val sak: Sak? = null,
    val dokumenter: List<ArkivDokument> = ArrayList()
)
