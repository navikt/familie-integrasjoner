package no.nav.familie.integrasjoner.aktør;

import no.nav.familie.integrasjoner.client.rest.PdlRestClient;
import no.nav.familie.kontrakter.felles.Tema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
public class AktørService {

    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private final PdlRestClient pdlRestClient;

    public AktørService(PdlRestClient pdlRestClient) {
        this.pdlRestClient = pdlRestClient;
    }

    @Cacheable(value = "aktør_personIdent_pdl", unless = "#result == null")
    public String getAktørIdFraPdl(String personIdent, Tema tema) {
        requireNonNull(personIdent, "personIdent");
        String responseFraRegister = pdlRestClient.hentGjeldendeAktørId(personIdent, tema);
        secureLogger.info("Legger fnr {} med aktørid {} i aktør-cache", personIdent, responseFraRegister);
        return responseFraRegister;
    }

}
