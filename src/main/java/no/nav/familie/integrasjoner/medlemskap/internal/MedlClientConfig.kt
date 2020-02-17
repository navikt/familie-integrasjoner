package no.nav.familie.integrasjoner.medlemskap.internal

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.http.sts.StsRestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MedlClientConfig {
    @Bean
    fun medlClient(@Value("\${MEDL2_URL}") url: String,
                   @Value("\${CREDENTIAL_USERNAME}") srvBruker: String,
                   @Autowired stsRestClient: StsRestClient,
                   @Autowired objectMapper: ObjectMapper): MedlClient {
        return MedlClient(url, srvBruker, stsRestClient, objectMapper)
    }
}