package no.nav.familie.ks.oppslag.config;


import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public PersonV3 personV3Port(@Value("${PERSON_V3_URL}") String personV3Url,
                                 @Value("${SECURITYTOKENSERVICE_URL}") String stsUrl,
                                 @Value("${CREDENTIAL_USERNAME}") String systemuserUsername,
                                 @Value("${CREDENTIAL_PASSWORD}") String systemuserPwd) {

        setSystemProperties(stsUrl, systemuserUsername, systemuserPwd);

        return new CXFClient<>(PersonV3.class)
                .address(personV3Url)
                .configureStsForSystemUser()
                .build();
    }

    @Bean
    public BehandleOppgaveV1 behandleOppgaveV1Port(@Value("${BEHANDLE_OPPGAVE_V1_URL}") String behandleOppgaveV1Url,
                                                   @Value("${SECURITYTOKENSERVICE_URL}") String stsUrl,
                                                   @Value("${CREDENTIAL_USERNAME}") String systemuserUsername,
                                                   @Value("${CREDENTIAL_PASSWORD}") String systemuserPwd) {

        setSystemProperties(stsUrl, systemuserUsername, systemuserPwd);

        return new CXFClient<>(BehandleOppgaveV1.class)
                .address(behandleOppgaveV1Url)
                .configureStsForSystemUser()
                .build();
    }

    @Bean
    public InnsynJournalV2 innsynJournalV2port(@Value("${INNSYN_JOURNAL_V2_URL}") String innsynJournalUrl,
                                               @Value("${SECURITYTOKENSERVICE_URL}") String stsUrl,
                                               @Value("${CREDENTIAL_USERNAME}") String systemuserUsername,
                                               @Value("${CREDENTIAL_PASSWORD}") String systemuserPwd) {

        setSystemProperties(stsUrl, systemuserUsername, systemuserPwd);
        return new CXFClient<>(InnsynJournalV2.class)
                .address(innsynJournalUrl)
                .configureStsForSystemUser()
                .build();
    }

    private void setSystemProperties(String stsUrl, String systemuserUsername, String systemuserPwd) {
        System.setProperty("no.nav.modig.security.sts.url", stsUrl);
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername);
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd);
    }
}
