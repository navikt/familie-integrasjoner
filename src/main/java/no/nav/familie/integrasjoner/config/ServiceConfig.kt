package no.nav.familie.integrasjoner.config

import no.nav.common.cxf.CXFClient
import no.nav.common.cxf.StsConfig
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig(
    @Value("\${SECURITYTOKENSERVICE_URL}") private val stsUrl: String,
    @Value("\${CREDENTIAL_USERNAME}") private val systemuserUsername: String,
    @Value("\${CREDENTIAL_PASSWORD}") private val systemuserPwd: String,
    @Value("\${ARBEIDSFORDELING_V1_URL}") private val arbeidsfordelingUrl: String,
    @Value("\${ORGANISASJON_V5_URL}") private val organisasjonV5Url: String
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
    fun organisasjonV5Port(): OrganisasjonV5 =
        CXFClient(OrganisasjonV5::class.java)
            .address(organisasjonV5Url)
            .timeout(20000, 20000)
            .configureStsForSystemUser(stsConfig())
            .build()

    @Bean
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 =
        CXFClient(ArbeidsfordelingV1::class.java)
            .address(arbeidsfordelingUrl)
            .configureStsForSystemUser(stsConfig())
            .build()
}
