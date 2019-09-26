package no.nav.familie.ks.oppslag.dokarkiv.client.domene;

public class AvsenderMottaker {
    private String id;

	private IdType idType;

	private String navn;

	public String getId() {
		return id;
	}

	public IdType getIdType() {
		return idType;
	}

	public String getNavn() {
		return navn;
	}

	public AvsenderMottaker(String id, IdType idType, String navn) {
		this.id = id;
		this.idType = idType;
		this.navn = navn;
	}
}
