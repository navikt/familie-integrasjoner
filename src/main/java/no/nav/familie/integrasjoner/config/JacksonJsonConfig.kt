package no.nav.familie.integrasjoner.config

import no.nav.familie.kontrakter.felles.jsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper

@Configuration
class JacksonJsonConfig {
    @Bean
    fun jsonMapper(): ObjectMapper = jsonMapper
}
