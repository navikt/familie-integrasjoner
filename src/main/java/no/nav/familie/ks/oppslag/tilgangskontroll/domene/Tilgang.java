package no.nav.familie.ks.oppslag.tilgangskontroll.domene;

import java.io.Serializable;
import java.util.Objects;

public class Tilgang implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean harTilgang;
    private String begrunnelse;

    public boolean isHarTilgang() {
        return harTilgang;
    }

    public Tilgang withHarTilgang(boolean harTilgang) {
        this.harTilgang = harTilgang;
        return this;
    }

    public void setHarTilgang(boolean harTilgang) {
        this.harTilgang = harTilgang;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public Tilgang withBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
        return this;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tilgang tilgang = (Tilgang) o;
        return harTilgang == tilgang.harTilgang &&
               Objects.equals(begrunnelse, tilgang.begrunnelse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(harTilgang, begrunnelse);
    }
}
