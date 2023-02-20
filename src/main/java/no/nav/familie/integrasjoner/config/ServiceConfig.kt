package no.nav.familie.integrasjoner.config

import no.nav.common.cxf.CXFClient
import no.nav.common.cxf.StsConfig
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig(
    @Value("\${SECURITYTOKENSERVICE_URL}") private val stsUrl: String,
    @Value("\${CREDENTIAL_USERNAME}") private val systemuserUsername: String,
    @Value("\${CREDENTIAL_PASSWORD}") private val systemuserPwd: String,
    @Value("\${ARBEIDSFORDELING_V1_URL}") private val arbeidsfordelingUrl: String,
) {

    init {
        System.setProperty("no.nav.modig.security.sts.url", stsUrl)
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername)
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd)
    }

    @Bean
    fun stsConfig(): StsConfig? {
        return StsConfig.builder()
            .url(stsUrl)
            .username(systemuserUsername)
            .password(systemuserPwd)
            .build()
    }

    @Bean
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 =
        CXFClient(ArbeidsfordelingV1::class.java)
            .address(arbeidsfordelingUrl)
            .configureStsForSystemUser(stsConfig())
            .build()
}
