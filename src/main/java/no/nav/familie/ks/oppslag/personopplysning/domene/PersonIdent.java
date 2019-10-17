package no.nav.familie.ks.oppslag.personopplysning.domene;

import java.util.Objects;

public class PersonIdent {

    private final String id;

    public PersonIdent(String personIdent) {
        Objects.requireNonNull(personIdent, "personIdent");
        this.id = personIdent;
    }

    public String getId() {
        return this.id;
    }
}
