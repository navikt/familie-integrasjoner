package no.nav.familie.integrasjoner.aktør.domene;

import java.util.List;

public class Aktør {

    private List<Ident> identer;
    private String feilmelding;

    public List<Ident> getIdenter() {
        return this.identer;
    }

    public String getFeilmelding() {
        return this.feilmelding;
    }

    public Aktør withIdenter(List<Ident> identer) {
        this.identer = identer;
        return this;
    }

    public Aktør withFeilmelding(String feilmelding) {
        this.feilmelding = feilmelding;
        return this;
    }

    @Override
    public String toString() {
        return "Aktør{" + "identer=" + identer +
               ", feilmelding=" + feilmelding +
               '}';
    }
}
