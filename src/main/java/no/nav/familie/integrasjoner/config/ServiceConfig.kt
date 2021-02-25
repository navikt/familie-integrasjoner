package no.nav.familie.integrasjoner.config

import no.nav.inf.GOSYSInfotrygdSak
import no.nav.sbl.dialogarena.common.cxf.CXFClient
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.binding.InfotrygdVedtakV1
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ext.WSPasswordCallback
import org.apache.wss4j.dom.WSConstants
import org.apache.wss4j.dom.handler.WSHandlerConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.security.auth.callback.CallbackHandler
import javax.xml.namespace.QName

@Configuration
class ServiceConfig(@Value("\${SECURITYTOKENSERVICE_URL}") stsUrl: String,
                    @Value("\${CREDENTIAL_USERNAME}") private val systemuserUsername: String,
                    @Value("\${CREDENTIAL_PASSWORD}") private val systemuserPwd: String,
                    @Value("\${PERSON_V3_URL}") private val personV3Url: String,
                    @Value("\${ARBEIDSFORDELING_V1_URL}") private val arbeidsfordelingUrl: String,
                    @Value("\${INFOTRYGD_VEDTAK_URL}") private val infotrygdVedtakUrl: String,
                    @Value("\${GOSYS_INFOTRYGDSAK_URL}") private val gosysInfotrygdSakUrl: String) {

    init {
        System.setProperty("no.nav.modig.security.sts.url", stsUrl)
        System.setProperty("no.nav.modig.security.systemuser.username", systemuserUsername)
        System.setProperty("no.nav.modig.security.systemuser.password", systemuserPwd)
    }

    @Bean
    fun personV3Port(): PersonV3 =
            CXFClient(PersonV3::class.java)
                    .address(personV3Url)
                    .timeout(20000, 20000)
                    .configureStsForSystemUser()
                    .build()

    @Bean
    fun gosysInfotrygdsakPort(): GOSYSInfotrygdSak =
            CXFClient(GOSYSInfotrygdSak::class.java)
                    .address(gosysInfotrygdSakUrl)
                    .wsdl("wsdl/cons-sak-gosys/wsdl/nav-cons-sak-gosys-3.0.0_GOSYSInfotrygdSakWSEXP.wsdl")
                    .serviceName(QName("http://nav-cons-sak-gosys-3.0.0/no/nav/inf/InfotrygdSak/Binding",
                                       "GOSYSInfotrygdSakWSEXP_GOSYSInfotrygdSakHttpService"))
                    .endpointName(QName("http://nav-cons-sak-gosys-3.0.0/no/nav/inf/InfotrygdSak/Binding",
                                        "GOSYSInfotrygdSakWSEXP_GOSYSInfotrygdSakHttpPort"))
                    .withOutInterceptor(WSS4JOutInterceptor(SecurityProps(systemuserUsername, systemuserPwd)))
                    .timeout(20000, 20000)
                    .build()

    @Bean
    fun arbeidsfordelingV1(): ArbeidsfordelingV1 =
            CXFClient(ArbeidsfordelingV1::class.java)
                    .address(arbeidsfordelingUrl)
                    .configureStsForSystemUser()
                    .build()

    @Bean
    fun infotrygdVedtak(): InfotrygdVedtakV1 =
            CXFClient(InfotrygdVedtakV1::class.java)
                    .address(infotrygdVedtakUrl)
                    .timeout(10000, 10000)
                    .configureStsForSystemUser()
                    .build()

}


class SecurityProps(user: String,
                    password: String) : HashMap<String, Any>() {

    init {
        this[WSHandlerConstants.ACTION] = WSHandlerConstants.USERNAME_TOKEN
        this[WSHandlerConstants.USER] = user
        this[WSHandlerConstants.PASSWORD_TYPE] = WSConstants.PW_TEXT
        this[WSHandlerConstants.PW_CALLBACK_REF] = CallbackHandler { callbacks ->
            val passwordCallback = callbacks[0] as WSPasswordCallback
            passwordCallback.password = password
        }
    }
}
