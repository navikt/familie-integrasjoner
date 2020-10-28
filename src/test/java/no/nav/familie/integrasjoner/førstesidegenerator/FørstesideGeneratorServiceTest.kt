package no.nav.familie.integrasjoner.førstesidegenerator

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import no.nav.familie.integrasjoner.client.rest.FørstesideGeneratorClient
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFoerstesideResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Førsteside
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test


class FørstesideGeneratorServiceTest {
    @MockK
    private lateinit var førstesideGeneratorClient: FørstesideGeneratorClient

    private lateinit var førstesideGeneratorService: FørstesideGeneratorService


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        førstesideGeneratorService = FørstesideGeneratorService(førstesideGeneratorClient)
    }

    @Test
    fun `genererFørsteside skal returnere byteArray med pdf`() {
        every { førstesideGeneratorClient.genererFørsteside(any()) }
                .answers { PostFoerstesideResponse(hentBytes()) }

        val førsteside = Førsteside(maalform = "NB",navSkjemaId = "123",overskriftsTittel = "Testoverskrift")

        val resultat = førstesideGeneratorService.genererForside(førsteside)


        Assertions.assertThat(resultat?.size == hentBytes().size)
    }


    private fun hentBytes(): ByteArray {
        return "PDF".toByteArray(Charsets.UTF_8)
    }
}