package no.nav.familie.ks.oppslag.journalpost.internal;

import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class InnsynJournalConsumerConfig {

    @Bean
    public InnsynJournalConsumer innsynJournalV2(InnsynJournalV2 innsynJournalV2) {
        return new InnsynJournalConsumer(innsynJournalV2);
    }
}
