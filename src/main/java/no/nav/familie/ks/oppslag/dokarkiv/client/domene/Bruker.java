package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

import javax.validation.constraints.NotNull;

public class Bruker {
    private IdType idType;
    private String id;

    public String getId() {
        return id;
    }

    public IdType getIdType() {
        return idType;
    }

    public Bruker(@NotNull(message = "Bruker mangler idType") IdType idType, @NotNull(message = "Bruker mangler id") String id) {
        this.idType = idType;
        this.id = id;
    }
}
