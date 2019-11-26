package no.nav.familie.integrasjoner.egenansatt.internal;


import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class EgenAnsattConsumerConfig {

    @Bean
    public EgenAnsattConsumer egenAnsattConsumer(EgenAnsattV1 egenAnsattV1) {
        return new EgenAnsattConsumer(egenAnsattV1);
    }
}
