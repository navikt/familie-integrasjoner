package no.nav.familie.integrasjoner.dokarkiv.metadata

import org.springframework.stereotype.Component

@Component
object OvergangsstønadSøknadMetadata : DokumentMetadata {

    override val tema: String = "ENF"
    override val behandlingstema: String? = "ab0071" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "OVERGANGSSTØNAD_SØKNAD"
    override val tittel: String? = "Søknad om overgangsstønad - enslig mor eller far"
    override val brevkode: String? = "NAV 15-00.01"
    override val dokumentKategori: String = "SOK"

}