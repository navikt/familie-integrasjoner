package no.nav.familie.ks.oppslag.aktør.internal;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;

import java.util.HashMap;

public class AktørResponse extends HashMap<String, Aktør> {

    public AktørResponse withAktør(String ident, Aktør aktør) {
        this.put(ident, aktør);
        return this;
    }
}
