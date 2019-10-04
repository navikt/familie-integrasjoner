package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.aktør.AktørService;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.TpsOversetter;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
@ApplicationScope
public class PersonopplysningerService {

    private static final Logger LOG = LoggerFactory.getLogger(PersonopplysningerService.class);
    private final PersonConsumer personConsumer;
    private AktørService aktørService;
    private TpsOversetter oversetter;

    @Autowired
    public PersonopplysningerService(AktørService aktørService, PersonConsumer personConsumer, TpsOversetter oversetter) {
        this.aktørService = aktørService;
        this.personConsumer = personConsumer;
        this.oversetter = oversetter;
    }

    ResponseEntity<PersonhistorikkInfo> hentHistorikkFor(AktørId aktørId, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");

        var personIdentResponse = aktørService.getPersonIdent(aktørId);
        try {
            String personIdent = Objects.requireNonNull(personIdentResponse.getBody());
            var request = new HentPersonhistorikkRequest();
            request.setAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)));
            var response = personConsumer.hentPersonhistorikkResponse(request);
            return ResponseEntity.ok(oversetter.tilPersonhistorikkInfo(aktørId.getId(), response));
        } catch (HentPersonhistorikkSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente historikk for person");
            return ResponseEntity.status(FORBIDDEN).header("message", exception.getMessage()).build();
        } catch (HentPersonhistorikkPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente historikk for person som ikke finnes i TPS");
            return ResponseEntity.status(NOT_FOUND).header("message", exception.getMessage()).build();
        } catch (NullPointerException npe) {
            LOG.info("Fant ikke fødselsnummer for aktørId {} i aktørreg. Klarer ikke hente historikk fra TPS", aktørId);
            return ResponseEntity.status(NOT_FOUND).header("message", hentEllerLagFeilmeldingForNPE(personIdentResponse)).build();
        }
    }

    ResponseEntity<Personinfo> hentPersoninfoFor(AktørId aktørId) {
        var personIdentResponse = aktørService.getPersonIdent(aktørId);
        try {
            String personIdent = Objects.requireNonNull(personIdentResponse.getBody());
            HentPersonRequest request = new HentPersonRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                    .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return ResponseEntity.ok(oversetter.tilPersoninfo(aktørId, response));
        } catch (HentPersonSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            return ResponseEntity.status(FORBIDDEN).header("message", exception.getMessage()).build();
        } catch (HentPersonPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            return ResponseEntity.status(NOT_FOUND).header("message", exception.getMessage()).build();
        } catch (NullPointerException npe) {
            LOG.info("Fant ikke fødselsnummer for aktørId {} i aktørreg. Klarer ikke hente personinfo fra TPS", aktørId);
            return ResponseEntity.status(NOT_FOUND).header("message", hentEllerLagFeilmeldingForNPE(personIdentResponse)).build();
        }
    }

    private String[] hentEllerLagFeilmeldingForNPE(ResponseEntity personIdentResponse) {
        return (String[]) Optional.ofNullable(personIdentResponse.getHeaders().get("message"))
                .orElse(List.of("aktørService::getPersonIdent returnerte null")).toArray();
    }
}
