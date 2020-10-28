package no.nav.familie.integrasjoner.førstesidegenerator.domene

import java.util.*


data class PostFoerstesideRequest (
        val spraakkode: Spraakkode = Spraakkode.NB,
        val adresse: Adresse? = null,
        val netsPostboks: String? = null,
        val avsender: Avsender? = null,
        val bruker: Bruker? = null,
        val ukjentBrukerPersoninfo: String? = null,
        val tema: String? = null,
        val behandlingstema: String? = null,
        val arkivtittel: String? = null,
        val vedleggsliste: List<String> = ArrayList(),
        val navSkjemaId: String? = null,
        val overskriftstittel: String? = null,
        val dokumentlisteFoersteside: List<String> = ArrayList(),
        val foerstesidetype: Foerstesidetype? = null,
        val enhetsnummer: String? = null,
        val arkivsak: Arkivsak? = null)
