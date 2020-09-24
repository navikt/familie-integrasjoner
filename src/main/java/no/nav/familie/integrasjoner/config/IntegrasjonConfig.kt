package no.nav.familie.integrasjoner.config

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.http.sts.StsRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URI

@Configuration
class IntegrasjonConfig {
    @Bean
    @Autowired
    @Profile("!mock-sts")
    fun stsRestClient(objectMapper: ObjectMapper,
                      @Value("\${STS_URL}") stsUrl: URI,
                      @Value("\${CREDENTIAL_USERNAME}") stsUsername: String,
                      @Value("\${CREDENTIAL_PASSWORD}") stsPassword: String): StsRestClient {
        val stsFullUrl = URI.create("$stsUrl?grant_type=client_credentials&scope=openid")
        return StsRestClient(objectMapper, stsFullUrl, stsUsername, stsPassword)
    }
}