package no.nav.familie.ks.oppslag.personopplysning;

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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@Service
@ApplicationScope
public class PersonopplysningerService {

    private static final Logger LOG = LoggerFactory.getLogger(PersonopplysningerService.class);
    public static final String PERSON = "PERSON";
    private final PersonConsumer personConsumer;
    private TpsOversetter oversetter;

    @Autowired
    public PersonopplysningerService(PersonConsumer personConsumer, TpsOversetter oversetter) {
        this.personConsumer = personConsumer;
        this.oversetter = oversetter;
    }

    ResponseEntity<PersonhistorikkInfo> hentHistorikkFor(String personIdent, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(personIdent, "personIdent");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        try {
            var request = new HentPersonhistorikkRequest();
            request.setAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)));
            var response = personConsumer.hentPersonhistorikkResponse(request);
            return ResponseEntity.ok(oversetter.tilPersonhistorikkInfo(new no.nav.familie.ks.oppslag.personopplysning.domene.PersonIdent(personIdent), response));
        } catch (HentPersonhistorikkSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente historikk for person");
            return ResponseEntity.status(FORBIDDEN).header("message", exception.getMessage()).build();
        } catch (HentPersonhistorikkPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente historikk for person som ikke finnes i TPS");
            return ResponseEntity.status(NOT_FOUND).header("message", exception.getMessage()).build();
        }
    }

    public ResponseEntity<Personinfo> hentPersoninfoFor(String personIdent) {
        try {
            HentPersonRequest request = new HentPersonRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                    .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return ResponseEntity.ok(oversetter.tilPersoninfo(new no.nav.familie.ks.oppslag.personopplysning.domene.PersonIdent(personIdent), response));
        } catch (HentPersonSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            return ResponseEntity.status(FORBIDDEN).header("message", exception.getMessage()).build();
        } catch (HentPersonPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            return ResponseEntity.status(NOT_FOUND).header("message", exception.getMessage()).build();
        }
    }

    @Cacheable(cacheNames = PERSON, key = "#personIdent", condition = "#personIdent != null")
    public Personinfo hentPersoninfo(String personIdent) {
            HentPersonRequest request = new HentPersonRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                    .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
        HentPersonResponse response = null;
        try {
            response = personConsumer.hentPersonResponse(request);
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            return null;
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            return null;
        }
        return oversetter.tilPersoninfo(new no.nav.familie.ks.oppslag.personopplysning.domene.PersonIdent(personIdent), response);
    }
}
