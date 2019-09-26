package no.nav.familie.ks.oppslag.dokarkiv.api;

import javax.validation.constraints.NotEmpty;

public class Dokument {
    @NotEmpty
    private byte[] dokument;
    @NotEmpty
    private FilType filType;

    private String filnavn;

    @NotEmpty
    private DokumentType dokumentType;

    public Dokument(@NotEmpty byte[] dokument, @NotEmpty FilType filType, String filnavn, @NotEmpty DokumentType dokumentType) {
        this.dokument = dokument;
        this.filType = filType;
        this.filnavn = filnavn;
        this.dokumentType = dokumentType;
    }

    public byte[] getDokument() {
        return dokument;
    }

    public FilType getFilType() {
        return filType;
    }

    public String getFilnavn() {
        return filnavn;
    }

    public DokumentType getDokumentType() {
        return dokumentType;
    }
}
