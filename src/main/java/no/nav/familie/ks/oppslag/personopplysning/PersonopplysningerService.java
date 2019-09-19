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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


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

        HttpStatus status;
        var feilmelding = new LinkedMultiValueMap<String, String>();

        var personIdentResponse = aktørService.getPersonIdent(aktørId);
        try {
            String personIdent = Objects.requireNonNull(personIdentResponse.getBody());
            var request = new HentPersonhistorikkRequest();
            request.setAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)));
            var response = personConsumer.hentPersonhistorikkResponse(request);
            return new ResponseEntity<>(oversetter.tilPersonhistorikkInfo(aktørId.getId(), response), HttpStatus.OK);
        } catch (HentPersonhistorikkSikkerhetsbegrensning hentPersonhistorikkSikkerhetsbegrensning) {
            LOG.info("Ikke tilgang til å hente historikk for person");
            status = HttpStatus.FORBIDDEN;
            feilmelding.add("message", hentPersonhistorikkSikkerhetsbegrensning.getMessage());
        } catch (HentPersonhistorikkPersonIkkeFunnet hentPersonhistorikkPersonIkkeFunnet) {
            LOG.info("Prøver å hente historikk for person som ikke finnes i TPS");
            status = HttpStatus.NOT_FOUND;
            feilmelding.add("message", hentPersonhistorikkPersonIkkeFunnet.getMessage());
        } catch (NullPointerException npe) {
            status = personIdentResponse.getStatusCode();
            feilmelding.addAll("message", Optional.ofNullable(personIdentResponse.getHeaders().get("message"))
                    .orElse(List.of("aktørService::getPersonIdent returnerte null"))
            );
        }
        return new ResponseEntity<>(feilmelding, status);
    }

    ResponseEntity<Personinfo> hentPersoninfoFor(AktørId aktørId) {
        HttpStatus status;
        var feilmelding = new LinkedMultiValueMap<String, String>();

        var personIdentResponse = aktørService.getPersonIdent(aktørId);
        try {
            String personIdent = Objects.requireNonNull(personIdentResponse.getBody());
            HentPersonRequest request = new HentPersonRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                    .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return new ResponseEntity<>(oversetter.tilPersoninfo(aktørId, response), HttpStatus.OK);
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            status = HttpStatus.FORBIDDEN;
            feilmelding.add("message", hentPersonSikkerhetsbegrensning.getMessage());
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            status = HttpStatus.NOT_FOUND;
            feilmelding.add("message", hentPersonPersonIkkeFunnet.getMessage());
        } catch (NullPointerException npe) {
            status = personIdentResponse.getStatusCode();
            feilmelding.addAll("message", Optional.ofNullable(personIdentResponse.getHeaders().get("message"))
                    .orElse(List.of("aktørService::getPersonIdent returnerte null"))
            );
        }
        return new ResponseEntity<>(feilmelding, status);
    }
}
