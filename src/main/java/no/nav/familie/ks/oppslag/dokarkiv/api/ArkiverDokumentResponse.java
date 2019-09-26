package no.nav.familie.ks.oppslag.dokarkiv.api;

public class ArkiverDokumentResponse {

    private String journalpostId;
    private Boolean ferdigstilt;

    public String getJournalpostId() {
        return journalpostId;
    }

    public boolean isFerdigstilt() {
        return ferdigstilt;
    }

    public ArkiverDokumentResponse(String journalpostId, Boolean ferdigstilt) {
        this.journalpostId = journalpostId;
        this.ferdigstilt = ferdigstilt;
    }
}
