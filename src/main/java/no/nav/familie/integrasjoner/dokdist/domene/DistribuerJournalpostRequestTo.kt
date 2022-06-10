package no.nav.familie.integrasjoner.dokdist.domene

import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstidspunkt
import no.nav.familie.kontrakter.felles.dokdist.Distribusjonstype

data class DistribuerJournalpostRequestTo(
    val journalpostId: String,
    val batchId: String? = null,
    val bestillendeFagsystem: String,
    val adresse: AdresseTo? = null,
    val dokumentProdApp: String,
    val distribusjonstype: Distribusjonstype?,
    val distribusjonstidspunkt: Distribusjonstidspunkt
)
