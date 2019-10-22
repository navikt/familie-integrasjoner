package no.nav.familie.ks.oppslag.personopplysning.domene;

import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.familie.ks.oppslag.personopplysning.domene.adresse.TpsAdresseOversetter;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.Familierelasjon;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.RelasjonsRolleType;
import no.nav.familie.ks.oppslag.personopplysning.domene.relasjon.SivilstandType;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusPeriode;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusType;
import no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet.Landkode;
import no.nav.familie.ks.oppslag.personopplysning.domene.tilhørighet.StatsborgerskapPeriode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class TpsOversetter {

    private TpsAdresseOversetter tpsAdresseOversetter;

    public TpsOversetter(TpsAdresseOversetter tpsAdresseOversetter) {
        this.tpsAdresseOversetter = tpsAdresseOversetter;
    }

    private Landkode utledLandkode(Statsborgerskap statsborgerskap) {
        Landkode landkode = Landkode.UDEFINERT;
        if (Optional.ofNullable(statsborgerskap).isPresent()) {
            landkode = new Landkode(statsborgerskap.getLand().getValue());
        }
        return landkode;
    }

    public Personinfo tilPersoninfo(PersonIdent personIdent, HentPersonResponse response) {
        Bruker person = (Bruker) response.getPerson();

        PersonIdent identFraTps = null;
        if (person.getAktoer() instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) {
            identFraTps = new PersonIdent(((no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) person.getAktoer()).getIdent().getIdent());
        }

        Set<Familierelasjon> familierelasjoner = person.getHarFraRolleI().stream()
                .map(this::tilRelasjon)
                .collect(toSet());

        String diskresjonskode = person.getDiskresjonskode() != null ? person.getDiskresjonskode().getValue() : null;
        String geografiskTilknytning = person.getGeografiskTilknytning() != null ? person.getGeografiskTilknytning().getGeografiskTilknytning() : null;

        return new Personinfo.Builder()
                .medPersonIdent(identFraTps != null ? identFraTps : personIdent)
                .medKjønn(person.getKjoenn().getKjoenn().getKodeRef())
                .medFamilierelasjon(familierelasjoner)
                .medBostedsadresse(tpsAdresseOversetter.finnBostedsadresseFor(person))
                .medAdresseLandkode(tpsAdresseOversetter.finnAdresseLandkodeFor(person))
                .medAdresseInfoList(tpsAdresseOversetter.lagListeMedAdresseInfo(person))
                .medUtlandsadresse(tpsAdresseOversetter.finnAdresseFor(person))
                .medPersonstatusType(tilPersonstatusType(person.getPersonstatus()))
                .medSivilstandType(tilSivilstandType(person.getSivilstand()))
                .medStatsborgerskap(utledLandkode(person.getStatsborgerskap()))
                .medFødselsdato(finnFødselsdato(person))
                .medDødsdato(finnDødsdato(person))
                .medDiskresjonsKode(diskresjonskode)
                .medGegrafiskTilknytning(geografiskTilknytning)
                .medNavn(person.getPersonnavn().getSammensattNavn())
                .build();
    }

    public PersonhistorikkInfo tilPersonhistorikkInfo(PersonIdent personIdent, HentPersonhistorikkResponse response) {

        PersonhistorikkInfo.Builder builder = PersonhistorikkInfo
                .builder()
                .medPersonIdent(personIdent);

        konverterPersonstatusPerioder(response, builder);

        konverterStatsborgerskapPerioder(response, builder);

        tpsAdresseOversetter.konverterBostedadressePerioder(response, builder);
        tpsAdresseOversetter.konverterPostadressePerioder(response, builder);
        tpsAdresseOversetter.konverterMidlertidigAdressePerioder(response, builder);

        return builder.build();
    }

    private void konverterPersonstatusPerioder(HentPersonhistorikkResponse response, PersonhistorikkInfo.Builder builder) {
        Optional.ofNullable(response.getPersonstatusListe()).ifPresent(list ->
                list.forEach(e -> {
                    Personstatus personstatus = new Personstatus();
                    personstatus.setPersonstatus(e.getPersonstatus());
                    PersonstatusType personstatusType = tilPersonstatusType(personstatus);

                    Periode gyldighetsperiode = Periode.innenfor(
                            DateUtil.convertToLocalDate(e.getPeriode().getFom()),
                            DateUtil.convertToLocalDate(e.getPeriode().getTom()));

                    PersonstatusPeriode periode = new PersonstatusPeriode(gyldighetsperiode, personstatusType);
                    builder.leggTil(periode);
                }));
    }

    private void konverterStatsborgerskapPerioder(HentPersonhistorikkResponse response, PersonhistorikkInfo.Builder builder) {
        Optional.ofNullable(response.getStatsborgerskapListe()).ifPresent(list -> list.forEach(e -> {
            Periode periode = Periode.innenfor(
                    DateUtil.convertToLocalDate(e.getPeriode().getFom()),
                    DateUtil.convertToLocalDate(e.getPeriode().getTom()));

            Landkode landkoder = new Landkode(e.getStatsborgerskap().getLand().getValue());
            StatsborgerskapPeriode element = new StatsborgerskapPeriode(periode, landkoder);
            builder.leggTil(element);
        }));
    }

    private PersonstatusType tilPersonstatusType(Personstatus personstatus) {
        return PersonstatusType.valueOf(personstatus.getPersonstatus().getValue());
    }

    private SivilstandType tilSivilstandType(Sivilstand sivilstand) {
        return SivilstandType.valueOf(sivilstand.getSivilstand().getValue());
    }

    private LocalDate finnDødsdato(Person person) {
        LocalDate dødsdato = null;
        Doedsdato dødsdatoJaxb = person.getDoedsdato();
        if (dødsdatoJaxb != null) {
            dødsdato = DateUtil.convertToLocalDate(dødsdatoJaxb.getDoedsdato());
        }
        return dødsdato;
    }

    private LocalDate finnFødselsdato(Person person) {
        LocalDate fødselsdato = null;
        Foedselsdato fødselsdatoJaxb = person.getFoedselsdato();
        if (fødselsdatoJaxb != null) {
            fødselsdato = DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return fødselsdato;
    }

    private Familierelasjon tilRelasjon(no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon familierelasjon) {
        String rollekode = familierelasjon.getTilRolle().getValue();
        RelasjonsRolleType relasjonsrolle = RelasjonsRolleType.valueOf(rollekode);
        PersonIdent personIdent = utledPersonIdent(familierelasjon);
        Boolean harSammeBosted = familierelasjon.isHarSammeBosted();

        return new Familierelasjon(personIdent, relasjonsrolle,
                tilLocalDate(familierelasjon.getTilPerson().getFoedselsdato()), harSammeBosted);
    }

    private PersonIdent utledPersonIdent(no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon familierelasjon) {
        final var aktoer = familierelasjon.getTilPerson().getAktoer();
        if (aktoer instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) {
            final var personIdent = ((no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent) aktoer).getIdent().getIdent();
            return new PersonIdent(personIdent);
        }

        throw new IllegalStateException("TPS returnerte noe annet enn PersonIdent for familierelasjon");
    }

    private LocalDate tilLocalDate(Foedselsdato fødselsdatoJaxb) {
        if (fødselsdatoJaxb != null) {
            return DateUtil.convertToLocalDate(fødselsdatoJaxb.getFoedselsdato());
        }
        return null;
    }
}
