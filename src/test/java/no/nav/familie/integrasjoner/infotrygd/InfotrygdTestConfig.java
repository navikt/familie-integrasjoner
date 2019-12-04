package no.nav.familie.integrasjoner.infotrygd;

import no.nav.familie.integrasjoner.client.rest.InfotrygdClient;
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Configuration
public class InfotrygdTestConfig {

    @Bean
    @Profile("mock-infotrygd")
    @Primary
    public InfotrygdClient infotrygdServiceMock() {
        InfotrygdClient infotrygdClientMock = mock(InfotrygdClient.class);
        var aktivKontantstøtteInfo = new AktivKontantstøtteInfo(false);
        when(infotrygdClientMock.hentAktivKontantstøtteFor(any())).thenReturn(aktivKontantstøtteInfo);
        doNothing().when(infotrygdClientMock).ping();
        return infotrygdClientMock;
    }
}
