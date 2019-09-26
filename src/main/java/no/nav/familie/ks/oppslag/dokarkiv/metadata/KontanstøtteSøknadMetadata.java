package no.nav.familie.ks.oppslag.dokarkiv.metadata;


public class KontanstøtteSøknadMetadata extends AbstractDokumentMetadata {
    public static final String TEMA = "KON";
    public static final String BEHANDLINGSTEMA = "ab0084"; // https://confluence.adeo.no/display/BOA/Behandlingstema
    public static final String KANAL = "NAV_NO";
    public static final String DOKUMENT_TYPE_ID = "KONTANTSTØTTE_SØKNAD";
    public static final String DOKUMENT_TITTEL = "Søknad om kontantstøtte til småbarnsforeldre";
    public static final String BREVKODE = "NAV 34-00.08";
    public static final String DOKUMENT_KATEGORI = "SOK";

    @Override
    public String getTema() {
        return TEMA;
    }

    @Override
    public String getBehandlingstema() {
        return BEHANDLINGSTEMA;
    }

    @Override
    public String getKanal() {
        return KANAL;
    }

    @Override
    public String getDokumentTypeId() {
        return DOKUMENT_TYPE_ID;
    }

    @Override
    public String getTittel() {
        return DOKUMENT_TITTEL;
    }

    @Override
    public String getBrevkode() {
        return BREVKODE;
    }

    @Override
    public String getDokumentKategori() {
        return DOKUMENT_KATEGORI;
    }
}
