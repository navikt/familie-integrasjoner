package no.nav.familie.ks.oppslag.medlemskap;

import no.nav.familie.ks.oppslag.felles.OppslagException;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsInfo;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsOversetter;
import no.nav.familie.ks.oppslag.medlemskap.domain.PeriodeInfo;
import no.nav.familie.ks.oppslag.medlemskap.domain.PeriodeStatus;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MedlemskapServiceTest {

    private static final String TEST_AKTØRID = "1000011111111";

    private MedlemskapService medlemskapService;
    private MedlClient medlClient;

    @Before
    public void setUp() throws Exception {
        medlClient = new MedlemskapTestConfig().medlClientMock();
        medlemskapService = new MedlemskapService(medlClient, new MedlemskapsOversetter());
    }

    @Test
    public void skal_gi_tomt_objekt_ved_ingen_treff_i_MEDL() {
        when(medlClient.hentMedlemskapsUnntakResponse(any())).thenReturn(Collections.emptyList());

        MedlemskapsInfo respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);

        assertThat(respons).isNotNull();
        assertThat(respons.getPersonIdent()).isEqualTo("");
        assertThat(respons.getAvvistePerioder()).isEqualTo(Collections.emptyList());
        assertThat(respons.getGyldigePerioder()).isEqualTo(Collections.emptyList());
        assertThat(respons.getUavklartePerioder()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void skal_gruppere_perioder_ved_treff() {
        MedlemskapsInfo respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);

        assertThat(respons).isNotNull();
        assertThat(respons.getUavklartePerioder().size()).isEqualTo(1);
        assertThat(respons.getGyldigePerioder().size()).isEqualTo(7);
        assertThat(respons.getAvvistePerioder().size()).isEqualTo(4);

        assertThat(respons.getGyldigePerioder().get(0).getPeriodeStatusÅrsak()).isNull();
        assertThat(respons.getAvvistePerioder().get(0).getPeriodeStatusÅrsak()).isNotNull();

        assertThat(respons.getGyldigePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.GYLD);
        assertThat(respons.getAvvistePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.AVST);
        assertThat(respons.getUavklartePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.UAVK);
    }

    @Test
    public void periodeinfo_har_påkrevde_felter() {
        MedlemskapsInfo respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);
        assertThat(respons).isNotNull();

        PeriodeInfo gyldigPeriode = respons.getGyldigePerioder().get(0);

        assertThat(gyldigPeriode.getFom()).isNotNull();
        assertThat(gyldigPeriode.getTom()).isNotNull();
        assertThat(gyldigPeriode.getGrunnlag()).isNotNull();
        assertThat(gyldigPeriode.isGjelderMedlemskapIFolketrygden()).isNotNull();
    }

    @Test(expected = OppslagException.class)
    public void skal_kaste_oppslagexception_ved_feil() {
        when(medlClient.hentMedlemskapsUnntakResponse(any())).thenThrow(new RuntimeException("Feil ved kall til MEDL2"));

        medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);
    }
}
