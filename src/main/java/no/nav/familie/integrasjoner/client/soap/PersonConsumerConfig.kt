package no.nav.familie.integrasjoner.client.soap

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties
class PersonConsumerConfig {

    @Bean
    fun personConsumer(personV3Port: PersonV3): PersonConsumer {
        return PersonConsumer(personV3Port)
    }
}