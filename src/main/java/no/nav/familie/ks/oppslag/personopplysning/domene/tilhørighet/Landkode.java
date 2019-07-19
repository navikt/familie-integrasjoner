package no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet;

import java.util.Objects;

public class Landkode {

    public static final Landkode UDEFINERT = new Landkode("UDEFINERT");
    public static final Landkode NORGE = new Landkode("NOR");

    private final String kode;

    public Landkode(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

    public boolean erNorge() {
        return NORGE.equals(this);
    }

    @Override
    public String toString() {
        return "Landkode{" +
                "kode='" + kode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Landkode landkode = (Landkode) o;
        return Objects.equals(kode, landkode.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }
}
