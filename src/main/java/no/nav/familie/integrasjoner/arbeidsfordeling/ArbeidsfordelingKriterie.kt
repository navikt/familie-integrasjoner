package no.nav.familie.integrasjoner.arbeidsfordeling

data class ArbeidsfordelingKriterie(
    val tema: String,
    val geografiskOmraade: String? = null,
    val diskresjonskode: String? = null,
    val skjermet: Boolean,
)
