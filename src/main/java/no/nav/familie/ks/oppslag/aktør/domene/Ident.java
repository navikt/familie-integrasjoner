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
}
