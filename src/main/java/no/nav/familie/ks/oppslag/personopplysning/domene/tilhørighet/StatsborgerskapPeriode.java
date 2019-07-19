package no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet;


import no.nav.familie.ks.oppslag.personopplysning.domene.Periode;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatsborgerskapPeriode periode = (StatsborgerskapPeriode) o;
        return Objects.equals(this.periode, periode.periode) &&
                Objects.equals(tilhørendeLand, periode.tilhørendeLand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, tilhørendeLand);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatsborgerskapPeriode{");
        sb.append("periode=").append(periode);
        sb.append(", tilhørendeLand=").append(tilhørendeLand);
        sb.append('}');
        return sb.toString();
    }
}
