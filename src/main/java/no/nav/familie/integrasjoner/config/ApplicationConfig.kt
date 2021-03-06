package no.nav.familie.integrasjoner.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@ComponentScan("no.nav.familie.integrasjoner")
@ConfigurationPropertiesScan
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger"])
@EnableOAuth2Client(cacheEnabled = true)
@EnableScheduling
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
}
