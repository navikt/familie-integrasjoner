package no.nav.familie.integrasjoner.infotrygd

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.InfotrygdRestClient
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class InfotrygdTestConfig {
    @Bean
    @Profile("mock-infotrygd")
    @Primary
    fun infotrygdRestClientMock(): InfotrygdRestClient {
        val infotrygdServiceMock: InfotrygdRestClient = mockk(relaxed = true)
        val aktivKontantstøtteInfo = AktivKontantstøtteInfo(false)
        every { infotrygdServiceMock.hentAktivKontantstøtteFor(any()) }
            .returns(aktivKontantstøtteInfo)
        return infotrygdServiceMock
    }
}
