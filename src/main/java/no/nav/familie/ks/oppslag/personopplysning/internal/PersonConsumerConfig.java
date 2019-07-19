package no.nav.familie.ks.oppslag.personopplysning.internal;


import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class PersonConsumerConfig {

    @Bean
    public PersonConsumer personConsumer(PersonV3 personV3Port) {
        return new PersonConsumer(personV3Port);
    }
}
