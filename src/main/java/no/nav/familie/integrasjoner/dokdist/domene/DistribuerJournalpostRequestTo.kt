package no.nav.familie.integrasjoner.dokdist.domene

data class DistribuerJournalpostRequestTo (val journalpostId: String,
                                           val batchId: String? = null,
                                           val bestillendeFagsystem: String,
                                           val adresse: AdresseTo? = null,
                                           val dokumentProdApp: String)