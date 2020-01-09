package no.nav.familie.integrasjoner.dokarkiv.client.domene

import java.util.*

class ArkivDokument(val tittel: String? = null,
                    val brevkode: String? = null,
                    val dokumentKategori: String? = null,
                    val dokumentvarianter: List<DokumentVariant> = ArrayList())
