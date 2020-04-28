package no.nav.familie.integrasjoner.config

import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.http.interceptor.StsBearerTokenClientInterceptor
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.log.filter.LogFilter
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
import org.springframework.core.env.Environment
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestOperations
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootConfiguration
@ComponentScan("no.nav.familie.integrasjoner")
@ConfigurationPropertiesScan
@EnableSwagger2
@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation.swagger.web.ApiResourceController"])
@EnableOAuth2Client(cacheEnabled = true)
@Import(ConsumerIdClientInterceptor::class, BearerTokenClientInterceptor::class, StsBearerTokenClientInterceptor::class)
class ApplicationConfig(
        private val environment: Environment
) {

    private val logger = LoggerFactory.getLogger(ApplicationConfig::class.java)

    @Bean("jwtBearer")
    fun restTemplateJwtBearer(consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              bearerTokenClientInterceptor: BearerTokenClientInterceptor): RestOperations {
        return if (trengerProxy()) {
            RestTemplateBuilder()
                    .additionalCustomizers(NaisProxyCustomizer())
                    .interceptors(consumerIdClientInterceptor,
                                  bearerTokenClientInterceptor,
                                  MdcValuesPropagatingClientInterceptor())
                    .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                    .build()
        } else {
            RestTemplateBuilder()
                    .interceptors(consumerIdClientInterceptor,
                                  bearerTokenClientInterceptor,
                                  MdcValuesPropagatingClientInterceptor())
                    .additionalMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
                    .build()
        }
    }

    @Bean("sts")
    fun restTemplateSts(stsBearerTokenClientInterceptor: StsBearerTokenClientInterceptor,
                        consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {

        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                              stsBearerTokenClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .requestFactory(this::requestFactory)
                .build()
    }

    fun requestFactory() = HttpComponentsClientHttpRequestFactory()
            .apply {
                setConnectionRequestTimeout(20 * 1000)
                setReadTimeout(20 * 1000)
                setConnectTimeout(20 * 1000)
            }


    private fun trengerProxy(): Boolean {
        return !environment.activeProfiles.any {
            listOf("e2e", "dev", "postgres", "integrasjonstest").contains(it.trim(' '))
        }
    }

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
