package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.domene.Ident;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import no.nav.familie.ks.oppslag.felles.OppslagException;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
public class AktørService {

    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private final CacheManager aktørCacheManager;
    private AktørregisterClient aktørregisterClient;

    @Autowired
    public AktørService(AktørregisterClient aktørregisterClient, CacheManager aktørCacheManager) {
        this.aktørCacheManager = aktørCacheManager;
        this.aktørregisterClient = aktørregisterClient;
    }

    public String getAktørId(String personIdent) {
        Objects.requireNonNull(personIdent, "personIdent");
        return Optional.ofNullable(aktørCache().get(personIdent)).orElseGet(() -> {
            var responseFraRegister = hentAktørIdFraRegister(personIdent);
            if (responseFraRegister != null) {
                secureLogger.info("Legger fnr {} med aktørid {} i aktør-cache", personIdent, responseFraRegister);
                aktørCache().put(personIdent, responseFraRegister);
            }
            return responseFraRegister;
        });
    }

    public String getPersonIdent(AktørId aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        return Optional.ofNullable(personIdentCache().get(aktørId.getId())).orElseGet(() -> {
            var responseFraRegister = hentPersonIdentFraRegister(aktørId);
            if (responseFraRegister != null) {
                secureLogger.info("Legger aktørid {} med fnr {} i personident-cache", aktørId.getId(), responseFraRegister);
                personIdentCache().put(aktørId.getId(), responseFraRegister);
            }
            return responseFraRegister;
        });
    }

    private Cache<String, String> aktørCache() {
        return aktørCacheManager.getCache("aktørIdCache", String.class, String.class);
    }

    private Cache<String, String> personIdentCache() {
        return aktørCacheManager.getCache("personIdentCache", String.class, String.class);
    }

    private String hentAktørIdFraRegister(String personIdent) {
        return fra(personIdent);
    }

    private String hentPersonIdentFraRegister(AktørId aktørId) {
        return fra(aktørId);
    }

    private <T> String fra(T idType) {
        boolean erAktørId = idType instanceof AktørId;
        String id = erAktørId ? ((AktørId) idType).getId() : (String) idType;

        Aktør response = erAktørId ?
                aktørregisterClient.hentPersonIdent(id).get(id) :
                aktørregisterClient.hentAktørId(id).get(id);

        secureLogger.info(erAktørId ? "Hentet fnr for aktørId: {}: {} fra aktørregisteret" : "Hentet aktør id'er for fnr: {}: {} fra aktørregisteret", id, response);

        if (response.getFeilmelding() == null) {
            final var identer = response.getIdenter().stream().filter(Ident::getGjeldende).collect(Collectors.toList());
            if (identer.size() == 1) {
                return identer.get(0).getIdent();
            } else {
                String melding = String.format("%s gjeldene %s", identer.isEmpty() ? "Ingen" : "Flere", erAktørId ? "aktørIder" : "norske identer");
                throw new OppslagException(melding, "aktør", OppslagException.Level.LAV, identer.isEmpty() ? NOT_FOUND : CONFLICT, null);
            }
        } else {
            throw new OppslagException(String.format("Funksjonell feil med følgende feilmelding: %s", response.getFeilmelding()), "aktør", OppslagException.Level.LAV, BAD_REQUEST, null);
        }
    }
}
