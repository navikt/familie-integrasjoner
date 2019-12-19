package no.nav.familie.integrasjoner.dokarkiv.client.domene

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class OpprettJournalpostRequest(var journalpostType: JournalpostType? = null,
                                var avsenderMottaker: AvsenderMottaker? = null,
                                var bruker: Bruker? = null,
                                var tema: String? = null,
                                var behandlingstema: String? = null,
                                var tittel: String? = null,
                                var kanal: String? = null,
                                var journalfoerendeEnhet: String? = null,
                                var eksternReferanseId: String? = null,
                                var sak: Sak? = null,
                                var dokumenter: List<ArkivDokument> = ArrayList())