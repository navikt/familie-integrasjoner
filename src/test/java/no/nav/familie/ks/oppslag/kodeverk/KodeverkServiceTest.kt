package no.nav.familie.ks.oppslag.kodeverk

import no.nav.familie.ef.mottak.api.kodeverk.KodeverkClient
import no.nav.familie.ef.mottak.api.kodeverk.KodeverkService
import no.nav.familie.ef.mottak.api.kodeverk.domene.BetydningerDto
import no.nav.familie.ef.mottak.api.kodeverk.domene.PostnummerDto
import no.nav.familie.ef.mottak.api.kodeverk.domene.PoststedDto
import no.nav.familie.ks.oppslag.kodeverk.domene.Språk
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.Mockito

class KodeverkServiceTest {
    private val kodeverkClientMock = Mockito.mock(KodeverkClient::class.java)
    private val kodeverkService = KodeverkService(kodeverkClientMock)

    @Test
    fun skalReturnerePoststed() {
        val poststedMock = PoststedDto(POSTSTED, "")
        val betydningerMock = BetydningerDto("", "", mapOf(Språk.BOKMÅL.kode to poststedMock))
        val postnummerMock = PostnummerDto(mapOf(POSTNUMMER to listOf(betydningerMock)))

        Mockito.`when`(kodeverkClientMock.hentPostnummerBetydninger()).thenReturn(postnummerMock)

        val poststedTest = kodeverkService.hentPoststedFor(POSTNUMMER)
        Assertions.assertThat(poststedTest).isEqualTo(POSTSTED)
    }

    companion object {
        private const val POSTNUMMER = "0557"
        private const val POSTSTED = "Oslo"
    }
}
