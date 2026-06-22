package no.nav.familie.integrasjoner.config

import no.nav.familie.log.NavSystemtype
import no.nav.familie.log.filter.LogFilter
import no.nav.familie.log.filter.RequestTimeFilter
import no.nav.familie.restklient.config.NaisProxyCustomizer
import no.nav.familie.restklient.config.RestTemplateSts
import no.nav.familie.sikkerhet.context.FamilieFellesSpringSecurityKonfigurasjon
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootConfiguration
@ComponentScan(
    "no.nav.familie.integrasjoner",
    "no.nav.familie.metrikker",
    "no.nav.familie.felles.tokenklient",
)
@ConfigurationPropertiesScan
@EnableScheduling
@Import(NaisProxyCustomizer::class, FamilieFellesSpringSecurityKonfigurasjon::class, RestTemplateSts::class)
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.setFilter(LogFilter(systemtype = NavSystemtype.NAV_INTEGRASJON))
        filterRegistration.order = 1
        return filterRegistration
    }

    @Bean
    fun requestTimeFilter(): FilterRegistrationBean<RequestTimeFilter> {
        logger.info("Registering RequestTimeFilter filter")
        val filterRegistration = FilterRegistrationBean<RequestTimeFilter>()
        filterRegistration.setFilter(RequestTimeFilter())
        filterRegistration.order = 2
        return filterRegistration
    }
}
