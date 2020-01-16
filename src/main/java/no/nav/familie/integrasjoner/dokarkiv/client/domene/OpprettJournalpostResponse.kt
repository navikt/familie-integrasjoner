package no.nav.familie.integrasjoner.dokarkiv.client.domene

class OpprettJournalpostResponse(val journalpostId: String? = null,
                                 val melding: String? = null,
                                 val journalpostferdigstilt: Boolean? = null,
                                 val dokumenter: List<DokumentInfo>? = null)
