package no.nav.familie.ks.oppslag.medlemskap;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class MedlemskapsUnntakResponse {

    @JsonProperty("dekning")
    private String dekning = null;

    @JsonProperty("fraOgMed")
    private Date fraOgMed = null;

    @JsonProperty("grunnlag")
    private String grunnlag = null;

    @JsonProperty("helsedel")
    private Boolean helsedel = null;

    @JsonProperty("ident")
    private String ident = null;

    @JsonProperty("lovvalg")
    private String lovvalg = null;

    @JsonProperty("lovvalgsland")
    private String lovvalgsland = null;

    @JsonProperty("medlem")
    private Boolean medlem = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("statusaarsak")
    private String statusaarsak = null;

    @JsonProperty("tilOgMed")
    private Date tilOgMed = null;

    @JsonProperty("unntakId")
    private Long unntakId = null;

    @JsonProperty("dekning")
    public String getDekning() {
        return dekning;
    }

    public void setDekning(String dekning) {
        this.dekning = dekning;
    }

    @JsonProperty("fraOgMed")
    @NotNull
    public Date getFraOgMed() {
        return fraOgMed;
    }

    public void setFraOgMed(Date fraOgMed) {
        this.fraOgMed = fraOgMed;
    }

    @JsonProperty("grunnlag")
    @NotNull
    public String getGrunnlag() {
        return grunnlag;
    }

    public void setGrunnlag(String grunnlag) {
        this.grunnlag = grunnlag;
    }

    @JsonProperty("helsedel")
    @NotNull
    public Boolean isHelsedel() {
        return helsedel;
    }

    public void setHelsedel(Boolean helsedel) {
        this.helsedel = helsedel;
    }

    @JsonProperty("ident")
    @NotNull
    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    @JsonProperty("lovvalg")
    @NotNull
    public String getLovvalg() {
        return lovvalg;
    }

    public void setLovvalg(String lovvalg) {
        this.lovvalg = lovvalg;
    }

    @JsonProperty("lovvalgsland")
    public String getLovvalgsland() {
        return lovvalgsland;
    }

    public void setLovvalgsland(String lovvalgsland) {
        this.lovvalgsland = lovvalgsland;
    }

    @JsonProperty("medlem")
    @NotNull
    public Boolean isMedlem() {
        return medlem;
    }

    public void setMedlem(Boolean medlem) {
        this.medlem = medlem;
    }

    @JsonProperty("status")
    @NotNull
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("statusaarsak")
    public String getStatusaarsak() {
        return statusaarsak;
    }

    public void setStatusaarsak(String statusaarsak) {
        this.statusaarsak = statusaarsak;
    }

    @JsonProperty("tilOgMed")
    @NotNull
    public Date getTilOgMed() {
        return tilOgMed;
    }

    public void setTilOgMed(Date tilOgMed) {
        this.tilOgMed = tilOgMed;
    }

    @JsonProperty("unntakId")
    @NotNull
    public Long getUnntakId() {
        return unntakId;
    }

    public void setUnntakId(Long unntakId) {
        this.unntakId = unntakId;
    }
}
