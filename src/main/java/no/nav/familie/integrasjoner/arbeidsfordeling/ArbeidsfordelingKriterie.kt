package no.nav.familie.integrasjoner.arbeidsfordeling

data class ArbeidsfordelingKritierie(val tema: String,
                                     val diskresjonskode: String? = null,
                                     val geografiskOmraade: String? = null,
                                     val skjermet: Boolean)
