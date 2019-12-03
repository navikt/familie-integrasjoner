package no.nav.familie.integrasjoner.client.soap

import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties
class EgenAnsattConsumerConfig {

    @Bean
    fun egenAnsattConsumer(egenAnsattV1: EgenAnsattV1): EgenAnsattConsumer {
        return EgenAnsattConsumer(egenAnsattV1)
    }
}