package no.nav.familie.integrasjoner.personopplysning;

import no.nav.familie.integrasjoner.client.rest.PdlRestClient;
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient;
import no.nav.familie.integrasjoner.felles.ws.DateUtil;
import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo;
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter;
import no.nav.familie.integrasjoner.personopplysning.internal.Person;
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
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;


@Service
@ApplicationScope
public class PersonopplysningerService {

    private static final Logger LOG = LoggerFactory.getLogger(PersonopplysningerService.class);
    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    public static final String PERSON = "PERSON";
    private final PdlRestClient pdlRestClient;
    private final PersonSoapClient personSoapClient;
    private TpsOversetter oversetter;

    @Autowired
    public PersonopplysningerService(PersonSoapClient personSoapClient, TpsOversetter oversetter, PdlRestClient pdlRestClient) {
        this.personSoapClient = personSoapClient;
        this.oversetter = oversetter;
        this.pdlRestClient = pdlRestClient;
    }

    PersonhistorikkInfo hentHistorikkFor(String personIdent, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(personIdent, "personIdent");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");

        var request = new HentPersonhistorikkRequest();
        request.setAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)));
        request.setPeriode(new Periode().withFom(DateUtil.convertToXMLGregorianCalendar(fom))
                                        .withTom(DateUtil.convertToXMLGregorianCalendar(tom)));
        var response = personSoapClient.hentPersonhistorikkResponse(request);
        return oversetter.tilPersonhistorikkInfo(
                new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(personIdent), response);
    }

    public Personinfo hentPersoninfoFor(String personIdent) {
        HentPersonRequest request = new HentPersonRequest()
                .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
        HentPersonResponse response = personSoapClient.hentPersonResponse(request);
        return oversetter.tilPersoninfo(new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(personIdent),
                                        response);
    }


    @Cacheable(cacheNames = PERSON, key = "#personIdent", condition = "#personIdent != null")
    public Personinfo hentPersoninfo(String personIdent) {
        HentPersonRequest request = new HentPersonRequest()
                .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(personIdent)))
                .withInformasjonsbehov(List.of(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE));
        HentPersonResponse response;

        response = personSoapClient.hentPersonResponse(request);

        return oversetter.tilPersoninfo(new no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent(personIdent),
                                        response);
    }

    public Person hentPersoninfo(String personIdent, String tema) {
        return pdlRestClient.hentPerson(personIdent, tema);
    }
}
