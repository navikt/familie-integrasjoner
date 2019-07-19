package no.nav.familie.ks.oppslag.personopplysning.domene.status;


import no.nav.familie.ks.oppslag.personopplysning.domene.Periode;

import java.util.Objects;

public class PersonstatusPeriode {

    private Periode periode;
    private PersonstatusType personstatus;

    public PersonstatusPeriode(Periode periode, PersonstatusType personstatus) {
        this.periode = periode;
        this.personstatus = personstatus;
    }

    public Periode getPeriode() {
        return this.periode;
    }

    public PersonstatusType getPersonstatus() {
        return this.personstatus;
    }

    @Override
    public String toString() {
        return "PersonstatusPeriode(periode=" + this.getPeriode()
            + ", personstatus=" + this.getPersonstatus()
            + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonstatusPeriode that = (PersonstatusPeriode) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(personstatus, that.personstatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, personstatus);
    }
}
