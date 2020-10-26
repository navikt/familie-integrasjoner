package no.nav.familie.integrasjoner.førstesidegenerator

import no.nav.familie.integrasjoner.client.rest.FørstesideGeneratorClient
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Foerstesidetype
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFoerstesideRequest
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Spraakkode
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service
@ApplicationScope
class FørstesideGeneratorService constructor(private val førstesideGeneratorClient: FørstesideGeneratorClient) {

    fun genererForside(): ByteArray? {
        val postFoerstesideRequest = PostFoerstesideRequest(
                spraakkode = Spraakkode.NB,
                navSkjemaId = "NAV 33.00-07",
                netsPostboks = "PB 1400",
                foerstesidetype = Foerstesidetype.ETTERSENDELSE,
                overskriftstittel = "Søknad om barnetrygd ved fødsel - NAV 33.00-07, Ettersendelse til søknad om barnetrygd ved fødsel - NAV 33.00-07",
                dokumentlisteFoersteside = arrayListOf(
                        "Søknad om barnetrygd ved fødsel",
                        "Dokumentasjon av inntekt"
                ),
                vedleggsliste = arrayListOf("Dokumentasjon av inntekt")
        )
        return førstesideGeneratorClient.genererFørsteside(postFoerstesideRequest)?.foersteside
    }
}