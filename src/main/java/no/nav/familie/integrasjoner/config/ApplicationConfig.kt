package no.nav.familie.integrasjoner.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.http.client.RetryOAuth2HttpClient
import no.nav.familie.http.config.NaisProxyCustomizer
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration
import java.time.temporal.ChronoUnit

@SpringBootConfiguration
@ComponentScan("no.nav.familie.integrasjoner")
@ConfigurationPropertiesScan
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableOAuth2Client(cacheEnabled = true)
@EnableScheduling
@Import(NaisProxyCustomizer::class)
class ApplicationConfig {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun kotlinModule(): KotlinModule = KotlinModule()

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        logger.info("Registering RequestTimeFilter filter")
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.filter = RequestTimeFilter()
        filterRegistration.order = 2
        return filterRegistration
    }

    @Primary
    @Bean
    fun oAuth2HttpClient(naisProxyCustomizer: NaisProxyCustomizer): OAuth2HttpClient {
        return RetryOAuth2HttpClient(
                RestTemplateBuilder()
                        .additionalCustomizers(naisProxyCustomizer)
                        .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                        .setReadTimeout(Duration.of(4, ChronoUnit.SECONDS))
        )
    }
}
