package no.nav.familie.integrasjoner.client.soap

import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties
class InnsynJournalConsumerConfig {

    @Bean
    fun innsynJournalV2(innsynJournalV2: InnsynJournalV2): InnsynJournalConsumer {
        return InnsynJournalConsumer(innsynJournalV2)
    }
}