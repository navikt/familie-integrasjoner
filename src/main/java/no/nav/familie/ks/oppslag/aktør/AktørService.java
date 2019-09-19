package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.domene.Ident;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public ResponseEntity<String> getAktørId(String personIdent) {
        Objects.requireNonNull(personIdent, "personIdent");
        return Optional.ofNullable(aktørCache().get(personIdent))
                .map(cachedPersonIdent -> new ResponseEntity<>(cachedPersonIdent, HttpStatus.OK)).orElseGet(() -> {
                    var responseFraRegister = hentAktørIdFraRegister(personIdent);
                    if (!responseFraRegister.getStatusCode().isError()) {
                        aktørCache().put(personIdent, responseFraRegister.getBody());
                    }
                    return responseFraRegister;
                });
    }

    public ResponseEntity<String> getPersonIdent(AktørId aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        return Optional.ofNullable(personIdentCache().get(aktørId))
                .map(cachedPersonIdent -> new ResponseEntity<>(cachedPersonIdent, HttpStatus.OK)).orElseGet(() -> {
                    var responseFraRegister = hentPersonIdentFraRegister(aktørId);
                    if (!responseFraRegister.getStatusCode().isError()) {
                        personIdentCache().put(aktørId, responseFraRegister.getBody());
                    }
                    return responseFraRegister;
                });
    }

    private Cache<String, String> aktørCache() {
        return aktørCacheManager.getCache("aktørIdCache", String.class, String.class);
    }

    private Cache<AktørId, String> personIdentCache() {
        return aktørCacheManager.getCache("personIdentCache", AktørId.class, String.class);
    }

    private ResponseEntity<String> hentAktørIdFraRegister(String personIdent) {
        return fra(personIdent);
    }

    private ResponseEntity<String> hentPersonIdentFraRegister(AktørId aktørId) {
        return fra(aktørId);

    }

    private <T> ResponseEntity<String> fra(T idType) {
        HttpStatus status;
        var feilmelding = new LinkedMultiValueMap<String, String>();

        boolean erAktørId = idType instanceof AktørId;
        String id = erAktørId ? ((AktørId) idType).getId() : (String) idType;

        Aktør response = erAktørId ?
                aktørregisterClient.hentPersonIdent(id).get(id) :
                aktørregisterClient.hentAktørId(id).get(id);

        secureLogger.info(erAktørId ? "Hentet fnr for aktørId: {}: {}" : "Hentet aktør id'er for fnr: {}: {}", id, response);

        if (response.getFeilmelding() == null) {
            final var identer = response.getIdenter().stream()
                    .filter(Ident::getGjeldende)
                    .collect(Collectors.toList());
            if (identer.size() == 1) {
                return new ResponseEntity<>(identer.get(0).getIdent(), HttpStatus.OK);
            } else {
                status = identer.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
                feilmelding.add("message",
                        String.format("%s gjeldene %s", identer.isEmpty() ? "Ingen" : "Flere", erAktørId ? "aktørIder" : "norske identer"));
            }
        } else {
            status = HttpStatus.BAD_REQUEST;
            feilmelding.add("message", String.format("Funksjonell feil. Fikk følgende feilmelding fra aktørregisteret: %s", response.getFeilmelding()));
        }
        return new ResponseEntity<>(feilmelding, status);
    }
}
