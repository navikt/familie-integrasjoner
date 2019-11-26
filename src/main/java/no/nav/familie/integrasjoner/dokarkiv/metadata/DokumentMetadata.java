package no.nav.familie.integrasjoner.dokarkiv.metadata;

public interface DokumentMetadata {
    String JOURNALFØRENDE_ENHET = "9999";

    String getTema();

    String getBehandlingstema();

    String getKanal();

    String getDokumentTypeId();

    String getTittel();

    String getBrevkode();

    String getDokumentKategori();
}
