package no.nav.familie.integrasjoner.dokarkiv.api


data class ArkiverDokumentRequest(val fnr: String,
                             val isForsøkFerdigstill: Boolean,
                             val dokumenter: List<Dokument>)
