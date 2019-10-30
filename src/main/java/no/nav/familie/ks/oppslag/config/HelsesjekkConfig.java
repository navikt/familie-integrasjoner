package no.nav.familie.ks.oppslag.config;

import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import no.nav.familie.ks.oppslag.egenansatt.internal.EgenAnsattConsumer;
import no.nav.familie.ks.oppslag.helse.*;
import no.nav.familie.ks.oppslag.infotrygd.InfotrygdService;
import no.nav.familie.ks.oppslag.journalpost.internal.InnsynJournalConsumer;
import no.nav.familie.ks.oppslag.journalpost.internal.SafKlient;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClient;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClientConfig;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveClient;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveConfig;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ServiceConfig.class, AktørClientConfig.class, MedlClientConfig.class, OppgaveConfig.class})
public class HelsesjekkConfig {

    @Bean
    public PersonV3Helsesjekk personV3Helsesjekk(PersonConsumer personConsumer) {
        return new PersonV3Helsesjekk(personConsumer);
    }

    @Bean
    public InnsynJournalV2Helsesjekk innsynJournalV2Helsesjekk(InnsynJournalConsumer innsynJournalConsumer) {
        return new InnsynJournalV2Helsesjekk(innsynJournalConsumer);
    }

    @Bean
    public EgenAnsattV1Helsesjekk egenAnsattV1Helsesjekk(EgenAnsattConsumer egenAnsattConsumer) {
        return new EgenAnsattV1Helsesjekk(egenAnsattConsumer);
    }

    @Bean
    public AktørHelsesjekk aktørHelsesjekk(AktørregisterClient aktørregisterClient) {
        return new AktørHelsesjekk(aktørregisterClient);
    }

    @Bean
    public MedlHelsesjekk medlHelsesjekk(MedlClient medlClient) {
        return new MedlHelsesjekk(medlClient);
    }

    @Bean
    public DokarkivHelsesjekk dokarkivHelsesjekk(DokarkivClient dokarkivClient) {
        return new DokarkivHelsesjekk(dokarkivClient);
    }

    @Bean
    public InfotrygdHelsesjekk infotrygdHelsesjekk(InfotrygdService infotrygdService) {
        return new InfotrygdHelsesjekk(infotrygdService);
    }

    @Bean
    public SafHelsesjekk safHelsesjekk(SafKlient safKlient) {
        return new SafHelsesjekk(safKlient);
    }

    @Bean
    public OppgaveHelsesjekk oppgaveHelsesjekk(OppgaveClient oppgaveClient) {
        return new OppgaveHelsesjekk(oppgaveClient);
    }
}
