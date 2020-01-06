package no.nav.familie.integrasjoner.dokarkiv.metadata

import org.springframework.stereotype.Component

@Component
object KontanstøtteSøknadVedleggMetadata : DokumentMetadata {

    override val tema: String = "KON"
    override val behandlingstema: String? = null // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = null
    override val dokumentTypeId: String = "KONTANTSTØTTE_SØKNAD_VEDLEGG"
    override val tittel: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: String = "IS"

}