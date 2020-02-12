package no.nav.familie.integrasjoner.personopplysning;

import no.nav.familie.integrasjoner.client.rest.PdlRestClient;
import no.nav.familie.integrasjoner.felles.ws.DateUtil;
import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo;
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter;
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsDato;
import no.nav.familie.integrasjoner.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Periode;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    public static final String PERSON = "PERSON";
    private final PersonConsumer personConsumer;
    private final PdlRestClient pdlRestClient;
    private TpsOversetter oversetter;

    @Autowired
    public PersonopplysningerService(PersonConsumer personConsumer, TpsOversetter oversetter, PdlRestClient pdlRestClient) {
        this.personConsumer = personConsumer;
        this.oversetter = oversetter;
        this.pdlRestClient = pdlRestClient;
    }

    PersonhistorikkInfo hentHistorikkFor(String personIdent, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(personIdent, "personIdent");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        try {
            var request = new HentPersonhistorikkRequest();
            request.setAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)));
            request.setPeriode(new Periode().withFom(DateUtil.convertToXMLGregorianCalendar(fom))
                                            .withTom(DateUtil.convertToXMLGregorianCalendar(tom)));
            var response = personConsumer.hentPersonhistorikkResponse(request);
            return oversetter.tilPersonhistorikkInfo(new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(
                    personIdent), response);
        } catch (HentPersonhistorikkSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente historikk for person");
            throw HttpClientErrorException.create(FORBIDDEN,
                                                  "Ikke tilgang til å hente historikk for person. " + exception.getMessage(),
                                                  null,
                                                  null,
                                                  null);
        } catch (HentPersonhistorikkPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente historikk for person som ikke finnes i TPS");
            throw HttpClientErrorException.create(NOT_FOUND,
                                                  "Kan ikke hente historikk for person som ikke finnes i TPS" +
                                                  exception.getMessage(),
                                                  null,
                                                  null,
                                                  null);
        }
    }

    public Personinfo hentPersoninfoFor(String personIdent) {
        try {
            HentPersonRequest request = new HentPersonRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                    .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return oversetter.tilPersoninfo(new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(personIdent),
                                            response);
        } catch (HentPersonSikkerhetsbegrensning exception) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            throw HttpClientErrorException.create(FORBIDDEN, exception.getMessage(), null, null, null);
        } catch (HentPersonPersonIkkeFunnet exception) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            throw HttpClientErrorException.create(NOT_FOUND, exception.getMessage(), null, null, null);
        }
    }


    @Cacheable(cacheNames = PERSON, key = "#personIdent", condition = "#personIdent != null")
    public Personinfo hentPersoninfo(String personIdent) {
        HentPersonRequest request = new HentPersonRequest()
                .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
        HentPersonResponse response;
        try {
            response = personConsumer.hentPersonResponse(request);
        } catch (HentPersonPersonIkkeFunnet hentPersonPersonIkkeFunnet) {
            LOG.info("Prøver å hente personinfo for person som ikke finnes i TPS");
            return null;
        } catch (HentPersonSikkerhetsbegrensning hentPersonSikkerhetsbegrensning) {
            LOG.info("Ikke tilgang til å hente personinfo for person");
            return null;
        }
        return oversetter.tilPersoninfo(new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(personIdent),
                                        response);
    }

    public PdlFødselsDato hentPersoninfo(String personIdent, String tema) {
        return pdlRestClient.hentFødselsdato(personIdent, tema);
    }
}
