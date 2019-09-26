package no.nav.familie.ks.oppslag.dokarkiv.metadata;


public class KontanstøtteSøknadVedleggMetadata extends AbstractDokumentMetadata {
    public static final String TEMA = "KON";
    public static final String BEHANDLINGSTEMA = null; // https://confluence.adeo.no/display/BOA/Behandlingstema
    public static final String DOKUMENT_TYPE_ID = "KONTANTSTØTTE_SØKNAD_VEDLEGG";
    public static final String DOKUMENT_KATEGORI = "IS";

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
        return null;
    }

    @Override
    public String getDokumentTypeId() {
        return DOKUMENT_TYPE_ID;
    }

    @Override
    public String getTittel() {
        return null;
    }

    @Override
    public String getBrevkode() {
        return null;
    }

    @Override
    public String getDokumentKategori() {
        return DOKUMENT_KATEGORI;
    }
}
