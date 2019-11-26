package no.nav.familie.integrasjoner.aktør.internal;

import no.nav.familie.integrasjoner.aktør.domene.Aktør;

import java.util.HashMap;

public class AktørResponse extends HashMap<String, Aktør> {

    public AktørResponse withAktør(String ident, Aktør aktør) {
        this.put(ident, aktør);
        return this;
    }
}
