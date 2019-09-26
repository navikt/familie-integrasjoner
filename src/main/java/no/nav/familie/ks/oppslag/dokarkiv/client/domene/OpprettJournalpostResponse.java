package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

import java.util.List;

public class OpprettJournalpostResponse {

	private String journalpostId;
	private String melding;
	private Boolean journalpostferdigstilt;
	private List<DokumentInfo> dokumenter;

	public String getJournalpostId() {
		return journalpostId;
	}

	public void setJournalpostId(String journalpostId) {
		this.journalpostId = journalpostId;
	}

	public String getMelding() {
		return melding;
	}

	public void setMelding(String melding) {
		this.melding = melding;
	}

	public Boolean getJournalpostferdigstilt() {
		return journalpostferdigstilt;
	}

	public void setJournalpostferdigstilt(Boolean journalpostferdigstilt) {
		this.journalpostferdigstilt = journalpostferdigstilt;
	}

	public List<DokumentInfo> getDokumenter() {
		return dokumenter;
	}

	public void setDokumenter(List<DokumentInfo> dokumenter) {
		this.dokumenter = dokumenter;
	}
}
