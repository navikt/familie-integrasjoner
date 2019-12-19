package no.nav.familie.integrasjoner.dokarkiv.client.domene

class OpprettJournalpostResponse(var journalpostId: String? = null,
                                 var melding: String? = null,
                                 var journalpostferdigstilt: Boolean? = null,
                                 var dokumenter: List<DokumentInfo>? = null)
