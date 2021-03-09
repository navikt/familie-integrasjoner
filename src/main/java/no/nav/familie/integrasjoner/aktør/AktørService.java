package no.nav.familie.integrasjoner.aktør;

import no.nav.familie.integrasjoner.aktør.domene.Aktør;
import no.nav.familie.integrasjoner.aktør.domene.Ident;
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient;
import no.nav.familie.integrasjoner.client.rest.PdlRestClient;
import no.nav.familie.integrasjoner.felles.OppslagException;
import no.nav.familie.integrasjoner.personopplysning.domene.AktørId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.*;

@Service
public class AktørService {

    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private final AktørregisterRestClient aktørregisterClient;
    private final PdlRestClient pdlRestClient;

    public AktørService(AktørregisterRestClient aktørregisterClient, PdlRestClient pdlRestClient) {
        this.aktørregisterClient = aktørregisterClient;
        this.pdlRestClient = pdlRestClient;
    }

    @Cacheable(value = "aktør_personIdent", unless = "#result == null")
    public String getAktørId(String personIdent) {
        requireNonNull(personIdent, "personIdent");
        String responseFraRegister = fra(personIdent);
        if (responseFraRegister != null) {
            secureLogger.info("Legger fnr {} med aktørid {} i aktør-cache", personIdent, responseFraRegister);
        }
        return responseFraRegister;
    }

    @Cacheable(value = "aktør_aktørId", unless = "#result == null")
    public String getPersonIdent(AktørId aktørId) {
        requireNonNull(aktørId, "aktørId");
        String responseFraRegister = fra(aktørId);
        if (responseFraRegister != null) {
            secureLogger.info("Legger aktørid {} med fnr {} i personident-cache", aktørId.getId(), responseFraRegister);
        }
        return responseFraRegister;
    }

    @Cacheable(value = "aktør_personIdent_pdl", unless = "#result == null")
    public String getAktørIdFraPdl(String personIdent, String tema) {
        requireNonNull(personIdent, "personIdent");
        String responseFraRegister = pdlRestClient.hentGjeldendeAktørId(personIdent, tema);
        secureLogger.info("Legger fnr {} med aktørid {} i aktør-cache", personIdent, responseFraRegister);
        return responseFraRegister;
    }

    @Cacheable(value = "aktør_aktørId_pdl", unless = "#result == null")
    public String getPersonIdentFraPdl(AktørId aktørId, String tema) {
        requireNonNull(aktørId, "aktørId");
        String responseFraRegister = pdlRestClient.hentGjeldendePersonident(aktørId.getId(), tema);
        secureLogger.info("Legger aktørid {} med fnr {} i personident-cache", aktørId.getId(), responseFraRegister);
        return responseFraRegister;
    }

    private <T> String fra(T idType) {
        boolean erAktørId = idType instanceof AktørId;
        String id = erAktørId ? ((AktørId) idType).getId() : (String) idType;

        Aktør response = erAktørId ?
                aktørregisterClient.hentPersonIdent(id).get(id) :
                aktørregisterClient.hentAktørId(id).get(id);

        secureLogger.info(erAktørId ?
                                  "Hentet fnr for aktørId: {}: {} fra aktørregisteret" :
                                  "Hentet aktør id'er for fnr: {}: {} fra aktørregisteret", id, response);

        if (response.getFeilmelding() == null) {
            final var identer = response.getIdenter().stream().filter(Ident::getGjeldende).collect(Collectors.toList());
            if (identer.size() == 1) {
                return identer.get(0).getIdent();
            }
            String melding = String.format("%s gjeldene %s",
                                           identer.isEmpty() ? "Ingen" : "Flere",
                                           erAktørId ? "aktørIder" : "norske identer");
            throw new OppslagException(melding,
                                       "aktør",
                                       OppslagException.Level.LAV,
                                       identer.isEmpty() ? NOT_FOUND : CONFLICT,
                                       null);
        }
        throw new OppslagException(String.format("Funksjonell feil med følgende feilmelding: %s", response.getFeilmelding()),
                                   "aktør",
                                   OppslagException.Level.LAV,
                                   BAD_REQUEST,
                                   null);
    }
}
