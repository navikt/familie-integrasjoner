package no.nav.familie.integrasjoner.config

import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig(@Value("\${SECURITYTOKENSERVICE_URL}") stsUrl: String,
                    @Value("\${CREDENTIAL_USERNAME}") systemuserUsername: String,
                    @Value("\${CREDENTIAL_PASSWORD}") systemuserPwd: String,
                    @Value("\${PERSON_V3_URL}") private val personV3Url: String,
                    @Value("\${INNSYN_JOURNAL_V2_URL}") private val innsynJournalUrl: String,
                    @Value("\${ARBEIDSFORDELING_V1_URL}") private val arbeidsfordelingUrl: String,
                    @Value("\${EGEN_ANSATT_V1_URL}") private val egenAnsattUrl: String) {

    init {
        System.setProperty("no.nav.modig.security.sts.url", stsUrl)
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername)
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd)
    }

    @Bean
    fun personV3Port(): PersonV3 =
            CXFClient(PersonV3::class.java)
                    .address(personV3Url)
                    .configureStsForSystemUser()
                    .build()

    @Bean
    fun innsynJournalV2port(): InnsynJournalV2 =
            CXFClient(InnsynJournalV2::class.java)
                    .address(innsynJournalUrl)
                    .configureStsForSystemUser()
                    .build()

    @Bean
    fun egenAnsattV1port(): EgenAnsattV1 =
            CXFClient(EgenAnsattV1::class.java)
                    .address(egenAnsattUrl)
                    .configureStsForSystemUser()
                    .build()

    @Bean
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 =
            CXFClient(ArbeidsfordelingV1::class.java)
                    .address(arbeidsfordelingUrl)
                    .configureStsForSystemUser()
                    .build()

}