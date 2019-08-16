package no.nav.familie.ks.oppslag.personopplysning.domene;


import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.Adresseinfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.Familierelasjon;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.SivilstandType;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusType;
import no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet.Landkode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Personinfo {

    private AktørId aktørId;
    private String navn;
    private String adresse;
    private LocalDate fødselsdato;
    private LocalDate dødsdato;
    private PersonstatusType personstatus;
    private SivilstandType sivilstand;
    private Set<Familierelasjon> familierelasjoner = Collections.emptySet();
    private Landkode statsborgerskap;
    private String utlandsadresse;
    private String geografiskTilknytning;
    private String diskresjonskode;
    private String adresseLandkode;

    private List<Adresseinfo> adresseInfoList = new ArrayList<>();

    private Personinfo() {
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public String getNavn() {
        return navn;
    }

    public PersonstatusType getPersonstatus() {
        return personstatus;
    }

    public SivilstandType getSivilstand() { return sivilstand; }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    public int getAlder() {
        return (int) ChronoUnit.YEARS.between(fødselsdato, LocalDate.now());
    }

    public Set<Familierelasjon> getFamilierelasjoner() {
        return Collections.unmodifiableSet(familierelasjoner);
    }

    public String getAdresse() {
        return adresse;
    }

    public LocalDate getDødsdato() {
        return dødsdato;
    }

    public Landkode getStatsborgerskap() {
        return statsborgerskap;
    }

    public String getUtlandsadresse() {
        return utlandsadresse;
    }

    public String getAdresseLandkode() {
        return adresseLandkode;
    }

    public String getGeografiskTilknytning() {
        return geografiskTilknytning;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    public List<Adresseinfo> getAdresseInfoList() {
        return adresseInfoList;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<aktørId=" + aktørId + ">"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static class Builder {
        private Personinfo personinfoMal;

        public Builder() {
            personinfoMal = new Personinfo();
        }

        public Builder medAktørId(AktørId aktørId) {
            personinfoMal.aktørId = aktørId;
            return this;
        }

        public Builder medNavn(String navn) {
            personinfoMal.navn = navn;
            return this;
        }

        public Builder medAdresse(String adresse) {
            personinfoMal.adresse = adresse;
            return this;
        }

        public Builder medFødselsdato(LocalDate fødselsdato) {
            personinfoMal.fødselsdato = fødselsdato;
            return this;
        }

        public Builder medDødsdato(LocalDate dødsdato) {
            personinfoMal.dødsdato = dødsdato;
            return this;
        }

        public Builder medPersonstatusType(PersonstatusType personstatus) {
            personinfoMal.personstatus = personstatus;
            return this;
        }

        public Builder medSivilstandType(SivilstandType sivilstand) {
            personinfoMal.sivilstand = sivilstand;
            return this;
        }

        public Builder medFamilierelasjon(Set<Familierelasjon> familierelasjon) {
            personinfoMal.familierelasjoner = familierelasjon;
            return this;
        }

        public Builder medStatsborgerskap(Landkode statsborgerskap) {
            personinfoMal.statsborgerskap = statsborgerskap;
            return this;
        }

        public Builder medUtlandsadresse(String utlandsadresse) {
            personinfoMal.utlandsadresse = utlandsadresse;
            return this;
        }

        public Builder medGegrafiskTilknytning(String geoTilkn) {
            personinfoMal.geografiskTilknytning = geoTilkn;
            return this;
        }

        public Builder medDiskresjonsKode(String diskresjonsKode) {
            personinfoMal.diskresjonskode = diskresjonsKode;
            return this;
        }

        public Builder medAdresseLandkode(String adresseLandkode) {
            personinfoMal.adresseLandkode = adresseLandkode;
            return this;
        }

        public Builder medAdresseInfoList(List<Adresseinfo> adresseinfoArrayList) {
            personinfoMal.adresseInfoList = adresseinfoArrayList;
            return this;
        }

        public Personinfo build() {
            requireNonNull(personinfoMal.aktørId, "Navbruker må ha aktørId"); //$NON-NLS-1$
            requireNonNull(personinfoMal.navn, "Navbruker må ha navn"); //$NON-NLS-1$
            requireNonNull(personinfoMal.fødselsdato, "Navbruker må ha fødselsdato"); //$NON-NLS-1$
            return personinfoMal;
        }

    }

}
