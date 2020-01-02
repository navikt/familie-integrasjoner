package no.nav.familie.integrasjoner.dokarkiv.metadata

import org.springframework.stereotype.Component

@Component
class KontanstøtteSøknadMetadata : AbstractDokumentMetadata() {

    override val tema: String = "KON"
    override val behandlingstema: String? = "ab0084" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "KONTANTSTØTTE_SØKNAD"
    override val tittel: String? = "Søknad om kontantstøtte til småbarnsforeldre"
    override val brevkode: String? = "NAV 34-00.08"
    override val dokumentKategori: String = "SOK"

}