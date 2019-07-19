package no.nav.familie.ks.oppslag.config;


import no.nav.sbl.dialogarena.common.cxf.CXFClient;
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

        System.setProperty("no.nav.modig.security.sts.url", stsUrl);
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername);
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd);

        return new CXFClient<>(PersonV3.class)
                .address(personV3Url)
                .configureStsForSystemUser()
                .build();
    }
}
