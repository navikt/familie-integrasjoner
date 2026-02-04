package no.nav.familie.integrasjoner.config

import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.restklient.sts.StsRestClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URI

@Configuration
class IntegrasjonConfig {
    @Bean
    @Profile("!mock-sts")
    fun stsRestClient(
        @Value("\${STS_URL}") stsUrl: URI?,
        @Value("\${CREDENTIAL_USERNAME}") stsUsername: String,
        @Value("\${CREDENTIAL_PASSWORD}") stsPassword: String,
    ): StsRestClient {
        val stsFullUrl = URI.create(stsUrl.toString() + "?grant_type=client_credentials&scope=openid")

        return StsRestClient(jsonMapper, stsFullUrl, stsUsername, stsPassword, null)
    }
}
