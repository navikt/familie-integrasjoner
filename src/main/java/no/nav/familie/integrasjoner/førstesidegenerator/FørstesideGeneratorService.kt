package no.nav.familie.integrasjoner.førstesidegenerator

import no.nav.familie.integrasjoner.client.rest.FørstesidegeneratorClient
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Adresse
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Bruker
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Brukertype
import no.nav.familie.integrasjoner.førstesidegenerator.domene.Førstesidetype
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFørstesideRequest
import no.nav.familie.kontrakter.felles.Språkkode
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.v2.Førsteside
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service
@ApplicationScope
class FørstesideGeneratorService(
    private val førstesidegeneratorClient: FørstesidegeneratorClient,
) {
    fun genererForside(
        førsteside: Førsteside,
        brukerId: String,
        tema: Tema,
    ): ByteArray {
        val postFørstesideRequest =
            PostFørstesideRequest(
                språkkode = førsteside.språkkode,
                adresse =
                    Adresse(
                        adresselinje1 = "Nav skanning",
                        adresselinje2 = "Postboks 1400",
                        postnummer = "0109",
                        poststed = "OSLO",
                    ),
                bruker =
                    Bruker(
                        brukerId = brukerId,
                        brukerType = Brukertype.PERSON,
                    ),
                navSkjemaId = førsteside.navSkjemaId, // NAV 33.00-07
                førstesidetype = Førstesidetype.ETTERSENDELSE,
                tema = tema.name,
                // "Søknad om barnetrygd ved fødsel - NAV 33.00-07,
                // Ettersendelse til søknad om barnetrygd ved fødsel - NAV 33.00-07",
                overskriftstittel = førsteside.overskriftstittel,
                dokumentlisteFørsteside = arrayListOf(vedleggstekst(førsteside.språkkode)),
                vedleggsliste = arrayListOf(vedleggstekst(førsteside.språkkode)),
            )
        return førstesidegeneratorClient.genererFørsteside(postFørstesideRequest).førsteside
    }

    fun vedleggstekst(språkkode: Språkkode) = if (språkkode == Språkkode.NN) Companion.VEDLAGTEBREV_TEKST_NN else Companion.VEDLAGTEBREV_TEKST_NB

    companion object {
        const val VEDLAGTEBREV_TEKST_NN = "Sjå vedlagte brev"
        const val VEDLAGTEBREV_TEKST_NB = "Se vedlagte brev"
    }
}
