package no.nav.familie.ks.oppslag.aktør.domene;

public class Ident {

    private String ident;
    private String identgruppe;
    private boolean gjeldende;

    public String getIdent() {
        return this.ident;
    }

    public String getIdentgruppe() {
        return this.identgruppe;
    }

    public boolean getGjeldende() {
        return this.gjeldende;
    }

    public Ident withIdent(String ident) {
        this.ident = ident;
        return this;
    }

    public Ident withGjeldende(boolean gjeldende) {
        this.gjeldende = gjeldende;
        return this;
    }

    @Override
    public String toString() {
        return "Ident{" + "ident=" + ident +
                ", identgruppe=" + identgruppe +
                ", gjeldende=" + gjeldende +
                '}';
    }
}
