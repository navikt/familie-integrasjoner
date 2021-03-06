package no.nav.familie.integrasjoner.førstesidegenerator

import no.nav.familie.integrasjoner.client.rest.FørstesideGeneratorClient
import no.nav.familie.integrasjoner.førstesidegenerator.domene.*
import no.nav.familie.kontrakter.felles.dokarkiv.Førsteside
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope


@Service
@ApplicationScope
class FørstesideGeneratorService constructor(private val førstesideGeneratorClient: FørstesideGeneratorClient) {

    fun genererForside(førsteside: Førsteside, brukerId: String): ByteArray? {
        val postFoerstesideRequest = PostFoerstesideRequest(
                spraakkode = Spraakkode.valueOf(førsteside.maalform),
                adresse = Adresse (
                        adresselinje1 = "Nav skanning",
                        adresselinje2 = "Postboks 1400",
                        postnummer = "0109",
                        poststed = "OSLO"
                ),
                bruker = Bruker (
                        brukerId = brukerId,
                        brukerType = BrukerType.PERSON
                ),
                navSkjemaId = førsteside.navSkjemaId, //NAV 33.00-07
                foerstesidetype = Foerstesidetype.ETTERSENDELSE,
                tema = "BAR", // TODO flytt ut i kontrakter
                overskriftstittel = førsteside.overskriftsTittel,//"Søknad om barnetrygd ved fødsel - NAV 33.00-07, Ettersendelse til søknad om barnetrygd ved fødsel - NAV 33.00-07",
                dokumentlisteFoersteside = arrayListOf(
                        if (førsteside.maalform == "NN") "Sjå vedlagte brev" else "Se vedlagte brev"
                ),
                vedleggsliste = arrayListOf(
                        if (førsteside.maalform == "NN") "Sjå vedlagte brev" else "Se vedlagte brev"
                )
        
        )
        return førstesideGeneratorClient.genererFørsteside(postFoerstesideRequest).foersteside
    }
}