package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.egenansatt.internal.EgenAnsattConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@Configuration
public class EgenAnsattTestConfig {

    @Bean
    @Profile("mock-egenansatt")
    @Primary
    public EgenAnsattConsumer egenAnsattConsumerMock() {
        EgenAnsattConsumer egenAnsattConsumer = mock(EgenAnsattConsumer.class);

        doNothing().when(egenAnsattConsumer).ping();
        return egenAnsattConsumer;
    }
}
