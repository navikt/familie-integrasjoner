package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpprettJournalpostRequest {

	private JournalpostType journalpostType;

	private AvsenderMottaker avsenderMottaker;

	private Bruker bruker;

	private String tema;

	private String behandlingstema;

	private String tittel;

	private String kanal;

	private String journalfoerendeEnhet;

	private String eksternReferanseId;

	private Sak sak;

	private List<Dokument> dokumenter = new ArrayList<>();

	public JournalpostType getJournalpostType() {
		return journalpostType;
	}

	public void setJournalpostType(JournalpostType journalpostType) {
		this.journalpostType = journalpostType;
	}

	public AvsenderMottaker getAvsenderMottaker() {
		return avsenderMottaker;
	}

	public void setAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		this.avsenderMottaker = avsenderMottaker;
	}

	public Bruker getBruker() {
		return bruker;
	}

	public void setBruker(Bruker bruker) {
		this.bruker = bruker;
	}

	public String getTema() {
		return tema;
	}

	public void setTema(String tema) {
		this.tema = tema;
	}

	public String getBehandlingstema() {
		return behandlingstema;
	}

	public void setBehandlingstema(String behandlingstema) {
		this.behandlingstema = behandlingstema;
	}

	public String getTittel() {
		return tittel;
	}

	public void setTittel(String tittel) {
		this.tittel = tittel;
	}

	public String getKanal() {
		return kanal;
	}

	public void setKanal(String kanal) {
		this.kanal = kanal;
	}

	public String getJournalfoerendeEnhet() {
		return journalfoerendeEnhet;
	}

	public void setJournalfoerendeEnhet(String journalfoerendeEnhet) {
		this.journalfoerendeEnhet = journalfoerendeEnhet;
	}

	public String getEksternReferanseId() {
		return eksternReferanseId;
	}

	public void setEksternReferanseId(String eksternReferanseId) {
		this.eksternReferanseId = eksternReferanseId;
	}

	public Sak getSak() {
		return sak;
	}

	public void setSak(Sak sak) {
		this.sak = sak;
	}

	public List<Dokument> getDokumenter() {
		return dokumenter;
	}

	public void setDokumenter(List<Dokument> dokumenter) {
		this.dokumenter = dokumenter;
	}


	public static final class OpprettJournalpostRequestBuilder {
		private JournalpostType journalpostType;
		private AvsenderMottaker avsenderMottaker;
		private Bruker bruker;
		private String tema;
		private String behandlingstema;
		private String tittel;
		private String kanal;
		private String journalfoerendeEnhet;
		private String eksternReferanseId;
		private Sak sak;
		private List<Dokument> dokumenter = new ArrayList<>();

		public OpprettJournalpostRequestBuilder() {
		}

		public static OpprettJournalpostRequestBuilder builder() {
			return new OpprettJournalpostRequestBuilder();
		}

		public OpprettJournalpostRequestBuilder medJournalpostType(JournalpostType journalpostType) {
			this.journalpostType = journalpostType;
			return this;
		}

		public OpprettJournalpostRequestBuilder medAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
			this.avsenderMottaker = avsenderMottaker;
			return this;
		}

		public OpprettJournalpostRequestBuilder medBruker(Bruker bruker) {
			this.bruker = bruker;
			return this;
		}

		public OpprettJournalpostRequestBuilder medTema(String tema) {
			this.tema = tema;
			return this;
		}

		public OpprettJournalpostRequestBuilder medBehandlingstema(String behandlingstema) {
			this.behandlingstema = behandlingstema;
			return this;
		}

		public OpprettJournalpostRequestBuilder medTittel(String tittel) {
			this.tittel = tittel;
			return this;
		}

		public OpprettJournalpostRequestBuilder medKanal(String kanal) {
			this.kanal = kanal;
			return this;
		}

		public OpprettJournalpostRequestBuilder medJournalfoerendeEnhet(String journalfoerendeEnhet) {
			this.journalfoerendeEnhet = journalfoerendeEnhet;
			return this;
		}

		public OpprettJournalpostRequestBuilder medEksternReferanseId(String eksternReferanseId) {
			this.eksternReferanseId = eksternReferanseId;
			return this;
		}

		public OpprettJournalpostRequestBuilder medSak(Sak sak) {
			this.sak = sak;
			return this;
		}

		public OpprettJournalpostRequestBuilder medDokumenter(List<Dokument> dokumenter) {
			this.dokumenter = dokumenter;
			return this;
		}

		public OpprettJournalpostRequest build() {
			OpprettJournalpostRequest opprettJournalpostRequest = new OpprettJournalpostRequest();
			opprettJournalpostRequest.setJournalpostType(journalpostType);
			opprettJournalpostRequest.setAvsenderMottaker(avsenderMottaker);
			opprettJournalpostRequest.setBruker(bruker);
			opprettJournalpostRequest.setTema(tema);
			opprettJournalpostRequest.setBehandlingstema(behandlingstema);
			opprettJournalpostRequest.setTittel(tittel);
			opprettJournalpostRequest.setKanal(kanal);
			opprettJournalpostRequest.setJournalfoerendeEnhet(journalfoerendeEnhet);
			opprettJournalpostRequest.setEksternReferanseId(eksternReferanseId);
			opprettJournalpostRequest.setSak(sak);
			opprettJournalpostRequest.setDokumenter(dokumenter);
			return opprettJournalpostRequest;
		}
	}
}
