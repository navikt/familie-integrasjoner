package no.nav.familie.ks.oppslag.journalpost.internal;

public class Sak {
    private String arkivsaksnummer;
    private String arkivsaksystem;

    public Sak() {
    }

    public Sak(String arkivsaksnummer, String arkivsaksystem) {
        this.arkivsaksnummer = arkivsaksnummer;
        this.arkivsaksystem = arkivsaksystem;
    }

    public String getArkivsaksnummer() {
        return arkivsaksnummer;
    }

    public String getArkivsaksystem() {
        return arkivsaksystem;
    }
}
