package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.domene.Ident;
import no.nav.familie.ks.oppslag.aktør.internal.AktørResponse;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String getAktørId(String personIdent) {
        return Optional.ofNullable(aktørCache().get(personIdent)).orElseGet(() -> {
            String aktørId = hentAktørIdFraRegister(personIdent);
            aktørCache().put(personIdent, aktørId);
            return aktørId;
        });
    }

    public String getPersonIdent(AktørId aktørId) {
        return Optional.ofNullable(personIdentCache().get(aktørId)).orElseGet(() -> {
            final var personIdent = hentPersonIdentFraRegister(aktørId);
            personIdentCache().put(aktørId, personIdent);
            return personIdent;
        });
    }

    private String hentAktørIdFraRegister(String personIdent) {
        AktørResponse response = aktørregisterClient.hentAktørId(personIdent);
        Aktør aktørResponse = response.get(personIdent);

        secureLogger.info("Hentet aktør id'er for fnr: {}: {}", personIdent, aktørResponse);
        if (aktørResponse.getFeilmelding() == null) {
            final var identer = aktørResponse.getIdenter()
                    .stream()
                    .filter(Ident::getGjeldende)
                    .collect(Collectors.toList());

            if(identer.size() > 1) {
                throw new IllegalStateException("Flere gjeldende aktørIder");
            } else if(identer.isEmpty()) {
                throw new IllegalStateException("Ingen gjeldene aktørIder");
            } else {
                return identer.get(0).getIdent();
            }
        } else {
            throw new RuntimeException(String.format("Feil ved kall mot Aktørregisteret. Feilmelding: %s",
                    aktørResponse.getFeilmelding())
            );
        }
    }

    private String hentPersonIdentFraRegister(AktørId aktørId) {
        final var ident = aktørId.getId();
        AktørResponse response = aktørregisterClient.hentPersonIdent(ident);
        Aktør aktørResponse = response.get(ident);

        secureLogger.info("Hentet fnr for aktørId: {}: {}", aktørId, aktørResponse);
        if (aktørResponse.getFeilmelding() == null) {
            final var identer = aktørResponse.getIdenter()
                    .stream()
                    .filter(Ident::getGjeldende)
                    .collect(Collectors.toList());

            if(identer.size() > 1) {
                throw new IllegalStateException("Flere gjeldende norske identer");
            } else if(identer.isEmpty()) {
                throw new IllegalStateException("Ingen gjeldene norske identer");
            } else {
                return identer.get(0).getIdent();
            }
        } else {
            throw new RuntimeException(String.format("Feil ved kall mot Aktørregisteret. Feilmelding: %s",
                    aktørResponse.getFeilmelding())
            );
        }
    }

    private Cache<String, String> aktørCache() {
        return aktørCacheManager.getCache("aktørIdCache", String.class, String.class);
    }

    private Cache<AktørId, String> personIdentCache() {
        return aktørCacheManager.getCache("personIdentCache", AktørId.class, String.class);
    }
}
