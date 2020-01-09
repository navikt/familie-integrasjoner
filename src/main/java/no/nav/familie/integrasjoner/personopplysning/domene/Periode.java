package no.nav.familie.integrasjoner.personopplysning.domene;

import no.nav.familie.integrasjoner.felles.ws.Tid;

import java.time.LocalDate;
import java.util.Objects;

public class Periode {

    private LocalDate fom;
    private LocalDate tom;

    private Periode(LocalDate fom, LocalDate tom) {
        // Fom er null om perioden for en opplysning gjelder fra personen ble født
        // Setter da fom til tidenes begynnelse for å slippe et ekstra kall for å hente fødselsdato
        LocalDate til = tom;
        LocalDate fra = fom;
        if (fra == null) {
            fra = Tid.TIDENES_BEGYNNELSE;
        }
        if (til == null) {
            til = Tid.TIDENES_ENDE;
        }

        this.fom = fra;
        this.tom = til;
    }

    public static Periode innenfor(LocalDate fom, LocalDate tom) {
        return new Periode(fom, tom);
    }

    public static Periode fraTilTidenesEnde(LocalDate fom) {
        return new Periode(fom, Tid.TIDENES_ENDE);
    }

    public LocalDate getFom() {
        return this.fom;
    }

    public LocalDate getTom() {
        return this.tom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Periode that = (Periode) o;
        return Objects.equals(fom, that.fom) &&
               Objects.equals(tom, that.tom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom);
    }

    @Override
    public String toString() {
        return "Periode{" + "fom=" + fom + ", tom=" + tom + '}';
    }
}
