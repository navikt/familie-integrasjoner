package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.domene.Ident;
import no.nav.familie.ks.oppslag.aktør.internal.AktørResponse;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AktørService {

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
        if (aktørResponse.getFeilmelding() == null) {
            return aktørResponse.getIdenter().get(0).getIdent();
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
        if (aktørResponse.getFeilmelding() == null) {
            return aktørResponse.getIdenter()
                    .stream()
                    .filter(Ident::getGjeldende)
                    .findFirst()
                    .map(Ident::getIdent)
                    .orElseThrow(() -> new RuntimeException("Fant ikke norskident for aktørId=" + ident));
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
