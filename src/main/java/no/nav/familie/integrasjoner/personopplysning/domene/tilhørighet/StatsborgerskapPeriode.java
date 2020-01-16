package no.nav.familie.integrasjoner.personopplysning.domene.tilhørighet;


import no.nav.familie.integrasjoner.personopplysning.domene.Periode;

import java.util.Objects;

public class StatsborgerskapPeriode {

    private Periode periode;
    private Landkode tilhørendeLand;

    public StatsborgerskapPeriode(Periode periode, Landkode statsborgerskap) {
        this.periode = periode;
        this.tilhørendeLand = statsborgerskap;
    }

    public Periode getPeriode() {
        return this.periode;
    }

    public Landkode getTilhørendeLand() {
        return this.tilhørendeLand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatsborgerskapPeriode statsborgerskapPeriode = (StatsborgerskapPeriode) o;
        return Objects.equals(this.periode, statsborgerskapPeriode.periode) &&
               Objects.equals(tilhørendeLand, statsborgerskapPeriode.tilhørendeLand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, tilhørendeLand);
    }

    @Override
    public String toString() {
        return "StatsborgerskapPeriode{" + "periode=" + periode +
               ", tilhørendeLand=" + tilhørendeLand +
               '}';
    }
}
