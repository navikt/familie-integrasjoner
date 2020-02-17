package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsInfo
import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsOversetter
import no.nav.familie.integrasjoner.medlemskap.domain.PeriodeStatus
import no.nav.familie.integrasjoner.medlemskap.domain.PeriodeStatusÅrsak
import no.nav.familie.integrasjoner.medlemskap.internal.MedlClient
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class MedlemskapServiceTest {
    private var medlemskapService: MedlemskapService? = null
    private var medlClient: MedlClient? = null
    @Before @Throws(Exception::class) fun setUp() {
        medlClient = MedlemskapTestConfig().medlClientMock()
        medlemskapService = MedlemskapService(medlClient!!, MedlemskapsOversetter())
    }

    @Test fun skal_gi_tomt_objekt_ved_ingen_treff_i_MEDL() {
        Mockito.`when`(medlClient!!.hentMedlemskapsUnntakResponse(
                ArgumentMatchers.any<String>())).thenReturn(emptyList())
        val respons = medlemskapService!!.hentMedlemskapsUnntak(TEST_AKTØRID)
        Assertions.assertThat<MedlemskapsInfo>(respons).isNotNull()
        Assertions.assertThat(respons!!.personIdent).isEqualTo("")
        Assertions.assertThat(respons.avvistePerioder).isEqualTo(emptyList<Any>())
        Assertions.assertThat(respons.gyldigePerioder).isEqualTo(emptyList<Any>())
        Assertions.assertThat(respons.uavklartePerioder).isEqualTo(emptyList<Any>())
    }

    @Test fun skal_gruppere_perioder_ved_treff() {
        val respons = medlemskapService!!.hentMedlemskapsUnntak(TEST_AKTØRID)
        Assertions.assertThat<MedlemskapsInfo>(respons).isNotNull()
        Assertions.assertThat(respons!!.uavklartePerioder!!.size).isEqualTo(1)
        Assertions.assertThat(respons.gyldigePerioder!!.size).isEqualTo(7)
        Assertions.assertThat(respons.avvistePerioder!!.size).isEqualTo(4)
        Assertions.assertThat<PeriodeStatusÅrsak>(respons.gyldigePerioder!![0].periodeStatusÅrsak).isNull()
        Assertions.assertThat<PeriodeStatusÅrsak>(respons.avvistePerioder!![0].periodeStatusÅrsak)
                .isNotNull()
        Assertions.assertThat<PeriodeStatus>(respons.gyldigePerioder!![0].periodeStatus)
                .isEqualTo(PeriodeStatus.GYLD)
        Assertions.assertThat<PeriodeStatus>(respons.avvistePerioder!![0].periodeStatus)
                .isEqualTo(PeriodeStatus.AVST)
        Assertions.assertThat<PeriodeStatus>(respons.uavklartePerioder!![0].periodeStatus)
                .isEqualTo(PeriodeStatus.UAVK)
    }

    @Test fun periodeinfo_har_påkrevde_felter() {
        val respons = medlemskapService!!.hentMedlemskapsUnntak(TEST_AKTØRID)
        Assertions.assertThat<MedlemskapsInfo>(respons).isNotNull()
        val gyldigPeriode = respons!!.gyldigePerioder!![0]
        Assertions.assertThat(gyldigPeriode.fom).isNotNull()
        Assertions.assertThat(gyldigPeriode.tom).isNotNull()
        Assertions.assertThat(gyldigPeriode.grunnlag).isNotNull()
        Assertions.assertThat(gyldigPeriode.isGjelderMedlemskapIFolketrygden).isNotNull()
    }

    @Test(expected = OppslagException::class) fun skal_kaste_oppslagexception_ved_feil() {
        Mockito.`when`(medlClient!!.hentMedlemskapsUnntakResponse(
                ArgumentMatchers.any<String>())).thenThrow(RuntimeException("Feil ved kall til MEDL2"))
        medlemskapService!!.hentMedlemskapsUnntak(TEST_AKTØRID)
    }

    companion object {
        private const val TEST_AKTØRID = "1000011111111"
    }
}