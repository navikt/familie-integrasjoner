package no.nav.familie.ks.oppslag.dokarkiv.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

public class ArkiverDokumentRequest {

    @NotBlank
    private String fnr;

    private boolean forsøkFerdigstill;
    @NotEmpty
    private List<Dokument> dokumenter;

    public ArkiverDokumentRequest() {
    }

    public ArkiverDokumentRequest(String fnr, String navn, boolean forsøkFerdigstill, List<Dokument> dokumenter) {
        this.fnr = fnr;
        this.forsøkFerdigstill = forsøkFerdigstill;
        this.dokumenter = dokumenter;
    }

    public String getFnr() {
        return fnr;
    }


    public boolean isForsøkFerdigstill() {
        return forsøkFerdigstill;
    }

    public List<Dokument> getDokumenter() {
        return dokumenter;
    }
}
