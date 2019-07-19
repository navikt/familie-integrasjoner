package no.nav.familie.ks.oppslag.personopplysning.domene.adresse;


import no.nav.familie.ks.oppslag.personopplysning.domene.Periode;

import java.util.Objects;

public class AdressePeriode {

    private Periode periode;
    private Adresse adresse;

    private AdressePeriode(Periode periode, Adresse adresse) {
        this.periode = periode;
        this.adresse = adresse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Periode getPeriode() {
        return periode;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdressePeriode that = (AdressePeriode) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(adresse, that.adresse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, adresse);
    }

    @Override
    public String toString() {
        return "AdressePeriode{" + "periode=" + periode +
                ", adresse=" + adresse +
                '}';
    }

    public static class Adresse {
        private AdresseType adresseType;
        private String adresselinje1;
        private String adresselinje2;
        private String adresselinje3;
        private String adresselinje4;
        private String postnummer;
        private String poststed;
        private String land;

        private Adresse() {
        }

        public AdresseType getAdresseType() {
            return adresseType;
        }

        public String getAdresselinje1() {
            return adresselinje1;
        }

        public String getAdresselinje2() {
            return adresselinje2;
        }

        public String getAdresselinje3() {
            return adresselinje3;
        }

        public String getAdresselinje4() {
            return adresselinje4;
        }

        public String getPostnummer() {
            return postnummer;
        }

        public String getPoststed() {
            return poststed;
        }

        public String getLand() {
            return land;
        }

        @Override
        public String toString() {
            return "Adresse{" + "adresseType=" + adresseType +
                    ", adresselinje1='" + adresselinje1 + '\'' +
                    ", adresselinje2='" + adresselinje2 + '\'' +
                    ", adresselinje3='" + adresselinje3 + '\'' +
                    ", adresselinje4='" + adresselinje4 + '\'' +
                    ", postnummer='" + postnummer + '\'' +
                    ", poststed='" + poststed + '\'' +
                    ", land='" + land + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Adresse adresse = (Adresse) o;
            return Objects.equals(adresseType, adresse.adresseType) &&
                    Objects.equals(adresselinje1, adresse.adresselinje1) &&
                    Objects.equals(adresselinje2, adresse.adresselinje2) &&
                    Objects.equals(adresselinje3, adresse.adresselinje3) &&
                    Objects.equals(adresselinje4, adresse.adresselinje4) &&
                    Objects.equals(postnummer, adresse.postnummer) &&
                    Objects.equals(poststed, adresse.poststed) &&
                    Objects.equals(land, adresse.land);
        }

        @Override
        public int hashCode() {
            return Objects.hash(adresseType, adresselinje1, adresselinje2, adresselinje3, adresselinje4, postnummer, poststed, land);
        }
    }


    public static final class Builder {
        private Periode periodeKladd;
        private Adresse adresseKladd;

        private Builder() {
            this.adresseKladd = new Adresse();
        }

        public Builder medGyldighetsperiode(Periode periode) {
            this.periodeKladd = periode;
            return this;
        }

        public Builder medAdresseType(AdresseType adresseType) {
            adresseKladd.adresseType = adresseType;
            return this;
        }

        public Builder medAdresselinje1(String adresselinje1) {
            adresseKladd.adresselinje1 = adresselinje1;
            return this;
        }

        public Builder medAdresselinje2(String adresselinje2) {
            adresseKladd.adresselinje2 = adresselinje2;
            return this;
        }

        public Builder medAdresselinje3(String adresselinje3) {
            adresseKladd.adresselinje3 = adresselinje3;
            return this;
        }

        public Builder medAdresselinje4(String adresselinje4) {
            adresseKladd.adresselinje4 = adresselinje4;
            return this;
        }

        public Builder medPostnummer(String postnummer) {
            adresseKladd.postnummer = postnummer;
            return this;
        }

        public Builder medPoststed(String poststed) {
            adresseKladd.poststed = poststed;
            return this;
        }

        public Builder medLand(String land) {
            adresseKladd.land = land;
            return this;
        }

        public AdressePeriode build() {
            return new AdressePeriode(periodeKladd, adresseKladd);
        }

    }
}
