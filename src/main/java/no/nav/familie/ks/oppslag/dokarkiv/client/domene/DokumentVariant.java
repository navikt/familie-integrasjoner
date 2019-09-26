package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

public class DokumentVariant {

	private String filtype;

	private String variantformat;

	private byte[] fysiskDokument;

	private String filnavn;

	public String getFiltype() {
		return filtype;
	}

	public String getVariantformat() {
		return variantformat;
	}

	public byte[] getFysiskDokument() {
		return fysiskDokument;
	}

	public String getFilnavn() {
		return filnavn;
	}

	public DokumentVariant(String filtype, String variantformat, byte[] fysiskDokument, String filnavn) {
		this.filtype = filtype;
		this.variantformat = variantformat;
		this.fysiskDokument = fysiskDokument;
		this.filnavn = filnavn;
	}
}
