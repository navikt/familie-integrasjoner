package no.nav.familie.ks.oppslag.infotrygd;

import no.nav.familie.ks.oppslag.infotrygd.domene.AktivKontantstøtteInfo;
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
    public InfotrygdService infotrygdServiceMock() {
        InfotrygdService infotrygdServiceMock = mock(InfotrygdService.class);
        var aktivKontantstøtteInfo = new AktivKontantstøtteInfo();
        aktivKontantstøtteInfo.setHarAktivKontantstotte(false);
        when(infotrygdServiceMock.hentAktivKontantstøtteFor(any())).thenReturn(aktivKontantstøtteInfo);
        doNothing().when(infotrygdServiceMock).ping();
        return infotrygdServiceMock;
    }
}
