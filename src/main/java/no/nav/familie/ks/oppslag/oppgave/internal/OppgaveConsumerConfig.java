package no.nav.familie.ks.oppslag.oppgave.internal;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class OppgaveConsumerConfig {

    @Bean
    public OppgaveConsumer oppgaveConsumer(BehandleOppgaveV1 behandleOppgaveV1Port) {
        return new OppgaveConsumer(behandleOppgaveV1Port);
    }
}
