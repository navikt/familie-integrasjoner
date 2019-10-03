package no.nav.familie.ks.oppslag.medlemskap;

import no.nav.familie.ks.oppslag.felles.OppslagException;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsInfo;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsOversetter;
import no.nav.familie.ks.oppslag.medlemskap.domain.PeriodeInfo;
import no.nav.familie.ks.oppslag.medlemskap.domain.PeriodeStatus;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MedlemskapServiceTest {

    private static final String TEST_AKTØRID = "1000011111111";

    private MedlemskapService medlemskapService;
    private MedlClient medlClient;

    @Before
    public void setUp() {
        medlClient = new MedlemskapTestConfig().medlClientMock();
        medlemskapService = new MedlemskapService(medlClient, new MedlemskapsOversetter());
    }

    @Test
    public void skal_gi_tomt_objekt_ved_ingen_treff_i_MEDL() {
        when(medlClient.hentMedlemskapsUnntakResponse(any())).thenReturn(Collections.emptyList());

        ResponseEntity<MedlemskapsInfo> respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isNotNull();
        assertThat(respons.getBody().getPersonIdent()).isEqualTo("");
        assertThat(respons.getBody().getAvvistePerioder()).isEqualTo(Collections.emptyList());
        assertThat(respons.getBody().getGyldigePerioder()).isEqualTo(Collections.emptyList());
        assertThat(respons.getBody().getUavklartePerioder()).isEqualTo(Collections.emptyList());
    }

    @Test
    public void skal_gruppere_perioder_ved_treff() {
        ResponseEntity<MedlemskapsInfo> respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respons.getBody()).isNotNull();
        assertThat(respons.getBody().getUavklartePerioder().size()).isEqualTo(1);
        assertThat(respons.getBody().getGyldigePerioder().size()).isEqualTo(7);
        assertThat(respons.getBody().getAvvistePerioder().size()).isEqualTo(4);

        assertThat(respons.getBody().getGyldigePerioder().get(0).getPeriodeStatusÅrsak()).isNull();
        assertThat(respons.getBody().getAvvistePerioder().get(0).getPeriodeStatusÅrsak()).isNotNull();

        assertThat(respons.getBody().getGyldigePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.GYLD);
        assertThat(respons.getBody().getAvvistePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.AVST);
        assertThat(respons.getBody().getUavklartePerioder().get(0).getPeriodeStatus()).isEqualTo(PeriodeStatus.UAVK);
    }

    @Test
    public void periodeinfo_har_påkrevde_felter() {
        ResponseEntity<MedlemskapsInfo> respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);
        assertThat(respons.getBody()).isNotNull();

        PeriodeInfo gyldigPeriode = respons.getBody().getGyldigePerioder().get(0);

        assertThat(gyldigPeriode.getFom()).isNotNull();
        assertThat(gyldigPeriode.getTom()).isNotNull();
        assertThat(gyldigPeriode.getGrunnlag()).isNotNull();
        assertThat(gyldigPeriode.isGjelderMedlemskapIFolketrygden()).isNotNull();
    }

    @Test(expected = OppslagException.class)
    public void skal_kaste_oppslagexception_ved_feil() {
        when(medlClient.hentMedlemskapsUnntakResponse(any())).thenThrow(new RuntimeException("Feil ved kall til MEDL2"));

        ResponseEntity<MedlemskapsInfo> respons = medlemskapService.hentMedlemskapsUnntak(TEST_AKTØRID);

        assertThat(respons.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
