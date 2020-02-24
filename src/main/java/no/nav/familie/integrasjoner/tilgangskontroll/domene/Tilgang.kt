package no.nav.familie.integrasjoner.tilgangskontroll.domene

data class Tilgang(val harTilgang: Boolean,
                   val begrunnelse: String? = null)
