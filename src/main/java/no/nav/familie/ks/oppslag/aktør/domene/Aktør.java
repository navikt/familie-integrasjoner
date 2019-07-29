package no.nav.familie.ks.oppslag.aktør.domene;

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
}
