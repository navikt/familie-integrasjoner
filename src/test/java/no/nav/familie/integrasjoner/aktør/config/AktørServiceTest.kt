package no.nav.familie.integrasjoner.aktør.config;

import no.nav.familie.integrasjoner.aktør.AktørService;
import no.nav.familie.integrasjoner.aktør.domene.Aktør;
import no.nav.familie.integrasjoner.aktør.domene.Ident;
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse;
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AktørServiceTest {

    private static final String PERSONIDENT = "11111111111";
    private static final String TESTAKTORID = "1000011111111";

    private CacheManager cacheManager = mock(CacheManager.class);
    private AktørregisterRestClient aktørClient = mock(AktørregisterRestClient.class);
    private AktørService aktørService;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        aktørService = new AktørService(aktørClient, cacheManager);
        when(cacheManager.getCache(any(), any(), any())).thenReturn(mock(Cache.class));
    }

    @Test
    public void skalReturnereAktørIdFraRegisterFørsteGang() {
        when(cacheManager.getCache(any(), any(), any()).get(anyString())).thenReturn(null);
        when(aktørClient.hentAktørId(anyString())).thenReturn(new AktørResponse()
                .withAktør(PERSONIDENT, new Aktør().withIdenter(
                        Collections.singletonList(new Ident().withIdent(TESTAKTORID).withGjeldende(true)))));

        String testAktørId = aktørService.getAktørId(PERSONIDENT);
        assertThat(testAktørId).isEqualTo(TESTAKTORID);
        verify(aktørClient, times(1)).hentAktørId(anyString());
    }

    @Test
    public void skalReturnereAktørIdFraCacheNaarDenLiggerDer() {
        when(cacheManager.getCache(any(), any(), any()).get(anyString())).thenReturn(TESTAKTORID);

        String testAktørId = aktørService.getAktørId(PERSONIDENT);
        assertThat(testAktørId).isEqualTo(TESTAKTORID);
        verifyZeroInteractions(aktørClient);
    }
}
