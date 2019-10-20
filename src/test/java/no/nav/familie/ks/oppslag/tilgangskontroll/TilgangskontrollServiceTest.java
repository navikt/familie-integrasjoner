package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.egenansatt.EgenAnsattService;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonIdent;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.tilgangskontroll.domene.Tilgang;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TilgangskontrollServiceTest {

    private static final String NAVN = "Navn Navnesen";
    private static final String FNR = "fnr";
    private static final PersonIdent PERSON_IDENT = new PersonIdent(FNR);

    @MockBean
    private EgenAnsattService egenAnsattService;
    private TilgangsKontrollService tilgangsKontrollService;


    @Before
    public void setUp() throws Exception {
        tilgangsKontrollService = new TilgangsKontrollService(egenAnsattService);
    }

    @Test
    public void tilgang_til_egen_ansatt_gir_ikke_tilgang_hvis_saksbehandler_mangler_rollen() throws Exception {
        when(egenAnsattService.erEgenAnsatt(any(String.class))).thenReturn(Boolean.TRUE);

        assertThat(tilgangsKontrollService.sjekkTilgang("123", "sdfsdf", this.personinfoUtenKode6og7()).isHarTilgang())
                .isEqualTo(new Tilgang().withHarTilgang(false).isHarTilgang());
    }

    @Test
    public void tilgang_til_egen_ansatt_gir_OK_hvis_søker_ikke_er_egen_ansatt() throws Exception {
        when(egenAnsattService.erEgenAnsatt(any(String.class))).thenReturn(Boolean.FALSE);

        assertThat(tilgangsKontrollService.sjekkTilgang("123", "sdfsdf", this.personinfoUtenKode6og7()).isHarTilgang())
                .isEqualTo(new Tilgang().withHarTilgang(true).isHarTilgang());
    }

    @Test
    public void hvis_kode6_har_ikke_saksbehandler_tilgang() throws Exception {
        when(egenAnsattService.erEgenAnsatt(any(String.class))).thenReturn(Boolean.FALSE);

        assertThat(tilgangsKontrollService.sjekkTilgang("123", "sdfsdf", this.personinfoMedKode6()).isHarTilgang())
                .isEqualTo(new Tilgang().withHarTilgang(false).isHarTilgang());
    }

    @Test
    public void hvis_kode7_har_ikke_saksbehandler_tilgang() throws Exception {
        when(egenAnsattService.erEgenAnsatt(any(String.class))).thenReturn(Boolean.FALSE);

        assertThat(tilgangsKontrollService.sjekkTilgang("123", "sdfsdf", this.personinfoMedKode7()).isHarTilgang())
                .isEqualTo(new Tilgang().withHarTilgang(false).isHarTilgang());
    }

    private Personinfo personinfoUtenKode6og7() {
        return new Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                       .medFødselsdato(LocalDate.now())
                                       .medNavn(NAVN)
                                       .medDiskresjonsKode("VANLIG_PERSON")
                                       .build();
    }

    private Personinfo personinfoMedKode6() {
        return new Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                       .medFødselsdato(LocalDate.now())
                                       .medNavn(NAVN)
                                       .medDiskresjonsKode(TilgangsKontrollService.DISKRESJONSKODE_KODE6)
                                       .build();
    }
    private Personinfo personinfoMedKode7() {
        return new Personinfo.Builder().medPersonIdent(PERSON_IDENT)
                                       .medFødselsdato(LocalDate.now())
                                       .medNavn(NAVN)
                                       .medDiskresjonsKode(TilgangsKontrollService.DISKRESJONSKODE_KODE7)
                                       .build();
    }
}
