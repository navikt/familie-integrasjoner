package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

import java.util.ArrayList;
import java.util.List;

public class Dokument {

	private String tittel;

	private String brevkode;

	private String dokumentKategori;

	public String getTittel() {
		return tittel;
	}

	public void setTittel(String tittel) {
		this.tittel = tittel;
	}

	public String getBrevkode() {
		return brevkode;
	}

	public void setBrevkode(String brevkode) {
		this.brevkode = brevkode;
	}

	public String getDokumentKategori() {
		return dokumentKategori;
	}

	public void setDokumentKategori(String dokumentKategori) {
		this.dokumentKategori = dokumentKategori;
	}

	public List<DokumentVariant> getDokumentvarianter() {
		return dokumentvarianter;
	}

	public void setDokumentvarianter(List<DokumentVariant> dokumentvarianter) {
		this.dokumentvarianter = dokumentvarianter;
	}

	private List<DokumentVariant> dokumentvarianter = new ArrayList<>();


	public static final class DokumentBuilder {
		private String tittel;
		private String brevkode;
		private String dokumentKategori;
		private List<DokumentVariant> dokumentvarianter = new ArrayList<>();

		private DokumentBuilder() {
		}

		public static DokumentBuilder aDokument() {
			return new DokumentBuilder();
		}

		public DokumentBuilder medTittel(String tittel) {
			this.tittel = tittel;
			return this;
		}

		public DokumentBuilder medBrevkode(String brevkode) {
			this.brevkode = brevkode;
			return this;
		}

		public DokumentBuilder medDokumentKategori(String dokumentKategori) {
			this.dokumentKategori = dokumentKategori;
			return this;
		}

		public DokumentBuilder medDokumentvarianter(List<DokumentVariant> dokumentvarianter) {
			this.dokumentvarianter = dokumentvarianter;
			return this;
		}

		public Dokument build() {
			Dokument dokument = new Dokument();
			dokument.tittel = tittel;
			dokument.brevkode = brevkode;
			dokument.dokumentKategori = dokumentKategori;
			dokument.dokumentvarianter = dokumentvarianter;
			return dokument;
		}
	}
}
