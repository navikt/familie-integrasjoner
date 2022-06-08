package no.nav.familie.integrasjoner.infotrygdsak

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.soap.InfotrygdsakSoapClient
import no.nav.gosys.asbo.infotrygdsak.ASBOGOSYSBestillInfotrygdSakResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class InfotrygdsakTestConfig {

    @Bean
    @Profile("mock-infotrygdsak")
    @Primary
    fun infotrygdsakSoapClientMock(): InfotrygdsakSoapClient {
        val infotrygdServiceMock: InfotrygdsakSoapClient = mockk(relaxed = true)
        val aktivKontantstøtteInfo = ASBOGOSYSBestillInfotrygdSakResponse()
        every { infotrygdServiceMock.opprettInfotrygdsak(any()) }
            .returns(aktivKontantstøtteInfo)
        return infotrygdServiceMock
    }
}
