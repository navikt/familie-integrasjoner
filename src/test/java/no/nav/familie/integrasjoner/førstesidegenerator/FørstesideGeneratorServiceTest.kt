package no.nav.familie.integrasjoner.førstesidegenerator

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import no.nav.familie.integrasjoner.client.rest.FørstesidegeneratorClient
import no.nav.familie.integrasjoner.førstesidegenerator.domene.PostFørstesideResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Førsteside
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FørstesideGeneratorServiceTest {

    @MockK
    private lateinit var førstesidegeneratorClient: FørstesidegeneratorClient

    private lateinit var førstesideGeneratorService: FørstesideGeneratorService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        førstesideGeneratorService = FørstesideGeneratorService(førstesidegeneratorClient)
    }

    @Test
    fun `genererFørsteside skal returnere byteArray med pdf`() {
        every { førstesidegeneratorClient.genererFørsteside(any()) }
            .answers { PostFørstesideResponse(hentBytes()) }

        val førsteside = Førsteside(maalform = "NB", navSkjemaId = "123", overskriftsTittel = "Testoverskrift")

        val resultat = førstesideGeneratorService.genererForside(førsteside, "123")

        Assertions.assertThat(resultat.size == hentBytes().size)
    }

    private fun hentBytes(): ByteArray {
        return "PDF".toByteArray(Charsets.UTF_8)
    }
}
