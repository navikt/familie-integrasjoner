package no.nav.familie.integrasjoner.medlemskap

import io.mockk.every
import no.nav.familie.integrasjoner.client.rest.MedlRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.medlemskap.PeriodeStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MedlemskapServiceTest {
    private lateinit var medlemskapService: MedlemskapService
    private lateinit var medlClient: MedlRestClient

    @BeforeEach
    fun setUp() {
        medlClient = MedlemskapTestConfig().medlClientMock()
        medlemskapService = MedlemskapService(medlClient)
    }

    @Test
    fun skal_gi_tomt_objekt_ved_ingen_treff_i_MEDL() {
        every { medlClient.hentMedlemskapsUnntakResponse(any()) }
            .returns(emptyList())

        val respons = medlemskapService.hentMedlemskapsunntak(TEST_AKTØRID)

        assertThat(respons).isNotNull
        assertThat(respons.personIdent).isEqualTo("")
        assertThat(respons.avvistePerioder).isEqualTo(emptyList<Any>())
        assertThat(respons.gyldigePerioder).isEqualTo(emptyList<Any>())
        assertThat(respons.uavklartePerioder).isEqualTo(emptyList<Any>())
    }

    @Test
    fun skal_gruppere_perioder_ved_treff() {
        val respons = medlemskapService.hentMedlemskapsunntak(TEST_AKTØRID)

        assertThat(respons).isNotNull
        assertThat(respons.uavklartePerioder.size).isEqualTo(1)
        assertThat(respons.gyldigePerioder.size).isEqualTo(7)
        assertThat(respons.avvistePerioder.size).isEqualTo(4)
        assertThat(respons.gyldigePerioder[0].periodeStatusÅrsak).isNull()
        assertThat(respons.avvistePerioder[0].periodeStatusÅrsak).isNotNull
        assertThat(respons.gyldigePerioder[0].periodeStatus).isEqualTo(PeriodeStatus.GYLD)
        assertThat(respons.avvistePerioder[0].periodeStatus).isEqualTo(PeriodeStatus.AVST)
        assertThat(respons.uavklartePerioder[0].periodeStatus).isEqualTo(PeriodeStatus.UAVK)
    }

    @Test
    fun `periodeInfo har påkrevde felter`() {
        val respons = medlemskapService.hentMedlemskapsunntak(TEST_AKTØRID)

        assertThat(respons).isNotNull
        val gyldigPeriode = respons.gyldigePerioder[0]
        assertThat(gyldigPeriode.fom).isNotNull()
        assertThat(gyldigPeriode.tom).isNotNull()
        assertThat(gyldigPeriode.grunnlag).isNotNull()
        assertThat(gyldigPeriode.gjelderMedlemskapIFolketrygden).isNotNull()
    }

    @Test
    fun `skal kaste oppslagException ved feil`() {
        every { medlClient.hentMedlemskapsUnntakResponse(any()) }
            .throws(RuntimeException("Feil ved kall til MEDL2"))

        assertThrows<OppslagException> { medlemskapService.hentMedlemskapsunntak(TEST_AKTØRID) }
    }

    companion object {
        private const val TEST_AKTØRID = "1000011111111"
    }
}
