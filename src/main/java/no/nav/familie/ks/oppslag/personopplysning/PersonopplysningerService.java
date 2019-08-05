package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.TpsOversetter;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerId;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import java.time.LocalDate;
import java.util.Objects;


@Service
@ApplicationScope
public class PersonopplysningerService {

    private final PersonConsumer personConsumer;
    private TpsOversetter oversetter;

    @Autowired
    public PersonopplysningerService(PersonConsumer personConsumer, TpsOversetter oversetter) {
        this.personConsumer = personConsumer;
        this.oversetter = oversetter;
    }

    public PersonhistorikkInfo hentHistorikkFor(AktørId aktørId, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        var request = new HentPersonhistorikkRequest();
        request.setAktoer(new AktoerId().withAktoerId(aktørId.getId()));
        try {
            var response = personConsumer.hentPersonhistorikkResponse(request);
            return oversetter.tilPersonhistorikkInfo(aktørId.getId(), response);
        } catch (HentPersonhistorikkSikkerhetsbegrensning hentPersonhistorikkSikkerhetsbegrensning) {
            throw new IllegalArgumentException(hentPersonhistorikkSikkerhetsbegrensning);
        } catch (HentPersonhistorikkPersonIkkeFunnet hentPersonhistorikkPersonIkkeFunnet) {
            // Fant ikke personen returnerer tomt sett
            return PersonhistorikkInfo.builder().medAktørId(aktørId.getId()).build();
        }
    }
}
