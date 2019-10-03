package no.nav.familie.ks.oppslag.medlemskap.domain;

import java.util.List;

public class MedlemskapsInfo {

    private String personIdent;
    private List<PeriodeInfo> gyldigePerioder;
    private List<PeriodeInfo> avvistePerioder;
    private List<PeriodeInfo> uavklartePerioder;

    public List<PeriodeInfo> getGyldigePerioder() {
        return gyldigePerioder;
    }

    public List<PeriodeInfo> getAvvistePerioder() {
        return avvistePerioder;
    }

    public List<PeriodeInfo> getUavklartePerioder() {
        return uavklartePerioder;
    }

    public String getPersonIdent() {
        return personIdent;
    }

    public static class Builder {
        private MedlemskapsInfo medlemskapsInfo;

        public Builder() {
            this.medlemskapsInfo = new MedlemskapsInfo();
        }

        public Builder medGyldigePerioder(List<PeriodeInfo> perioder) {
            medlemskapsInfo.gyldigePerioder = perioder;
            return this;
        }

        public Builder medAvvistePerioder(List<PeriodeInfo> perioder) {
            medlemskapsInfo.avvistePerioder = perioder;
            return this;
        }

        public Builder medUavklartePerioder(List<PeriodeInfo> perioder) {
            medlemskapsInfo.uavklartePerioder = perioder;
            return this;
        }

        public Builder medPersonIdent(String personIdent) {
            medlemskapsInfo.personIdent = personIdent;
            return this;
        }

        public MedlemskapsInfo build() {
            return medlemskapsInfo;
        }
    }
}
