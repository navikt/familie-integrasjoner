package no.nav.familie.integrasjoner.personopplysning.domene.relasjon;

import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent;

import java.time.LocalDate;

public class Familierelasjon {

    private PersonIdent personIdent;
    private RelasjonsRolleType relasjonsrolle;
    private LocalDate fødselsdato;
    private Boolean harSammeBosted;

    public Familierelasjon(PersonIdent personIdent, RelasjonsRolleType relasjonsrolle, LocalDate fødselsdato,
                           Boolean harSammeBosted) {
        this.personIdent = personIdent;
        this.relasjonsrolle = relasjonsrolle;
        this.fødselsdato = fødselsdato;
        this.harSammeBosted = harSammeBosted;
    }

    public RelasjonsRolleType getRelasjonsrolle() {
        return relasjonsrolle;
    }

    public Boolean getHarSammeBosted() {
        return harSammeBosted;
    }

    public PersonIdent getPersonIdent() {
        return personIdent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
               + "<relasjon=" + relasjonsrolle
               + ", fødselsdato=" + fødselsdato
               + ">";
    }
}
