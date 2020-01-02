package no.nav.familie.integrasjoner.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.http.interceptor.StsBearerTokenClientInterceptor
import no.nav.familie.http.interceptor.TimingAndLoggingClientHttpRequestInterceptor
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.interceptor.AzureBearerTokenClientInterceptor
import no.nav.familie.log.filter.LogFilter
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestOperations
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootConfiguration
@ComponentScan("no.nav.familie")
@ConfigurationPropertiesScan
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
@EnableOAuth2Client(cacheEnabled = true)
@EnableSwagger2
class ApplicationConfig {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean("azure")
    fun restTemplateAzureAd(interceptorAzure: AzureBearerTokenClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .interceptors(interceptorAzure)
                .requestFactory(this::requestFactory)
                .build()
    }

    @Bean("sts")
    fun restTemplateSts(@Value("\${CREDENTIAL_USERNAME}") consumer: String,
                        stsRestClient: StsRestClient): RestOperations {

        return RestTemplateBuilder()
                .interceptors(ConsumerIdClientInterceptor(consumer),
                              StsBearerTokenClientInterceptor(stsRestClient),
                              MdcValuesPropagatingClientInterceptor(),
                              TimingAndLoggingClientHttpRequestInterceptor())
                .requestFactory(this::requestFactory)
                .build()
    }

    fun requestFactory() = HttpComponentsClientHttpRequestFactory()
            .apply {
                setConnectionRequestTimeout(20 * 1000)
                setReadTimeout(20 * 1000)
                setConnectTimeout(20 * 1000)
            }

    @Bean
    fun kotlinModule(): KotlinModule {
        return KotlinModule()
    }

    @Bean
    fun logFilter(): FilterRegistrationBean<LogFilter> {
        logger.info("Registering LogFilter filter")
        val filterRegistration = FilterRegistrationBean<LogFilter>()
        filterRegistration.filter = LogFilter()
        filterRegistration.order = 1
        return filterRegistration
    }

}