package no.nav.familie.integrasjoner.config

import no.nav.familie.http.config.NaisProxyCustomizer
import no.nav.familie.http.interceptor.BearerTokenClientCredentialsClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenClientInterceptor
import no.nav.familie.http.interceptor.BearerTokenWithSTSFallbackClientInterceptor
import no.nav.familie.http.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.http.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.http.interceptor.StsBearerTokenClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@Import(
    ConsumerIdClientInterceptor::class,
    BearerTokenClientInterceptor::class,
    StsBearerTokenClientInterceptor::class,
    BearerTokenWithSTSFallbackClientInterceptor::class,
    BearerTokenClientCredentialsClientInterceptor::class,
)
class RestTemplateConfig(
    private val environment: Environment,
) {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun restTemplateMedProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplate =
        RestTemplateBuilder()
            .medProxy(naisProxyCustomizer)
            .build()

    @Bean
    fun restTemplateBuilderMedProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplateBuilder =
        RestTemplateBuilder()
            .medProxy(naisProxyCustomizer)

    /**
     * Denne bruker jwt-bearer hvis den finnes, hvis ikke så bruker den client_credentials
     */
    @Bean("jwtBearer")
    fun restTemplateJwtBearer(
        naisProxyCustomizer: NaisProxyCustomizer,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientInterceptor: BearerTokenClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .medProxy(naisProxyCustomizer)
            .interceptors(
                consumerIdClientInterceptor,
                bearerTokenClientInterceptor,
                MdcValuesPropagatingClientInterceptor(),
            ).setConnectTimeout(Duration.ofSeconds(20))
            .setReadTimeout(Duration.ofSeconds(20))
            .build()

    @Bean("clientCredential")
    fun restTemplateClientCredential(
        naisProxyCustomizer: NaisProxyCustomizer,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenClientInterceptor: BearerTokenClientCredentialsClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .medProxy(naisProxyCustomizer)
            .interceptors(
                consumerIdClientInterceptor,
                bearerTokenClientInterceptor,
                MdcValuesPropagatingClientInterceptor(),
            ).setConnectTimeout(Duration.ofSeconds(20))
            .setReadTimeout(Duration.ofSeconds(20))
            .build()

    @Bean("jwtBearerOboOgSts")
    fun restTemplateOboOgSts(
        naisProxyCustomizer: NaisProxyCustomizer,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
        bearerTokenWithSTSFallbackClientInterceptor: BearerTokenWithSTSFallbackClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .medProxy(naisProxyCustomizer)
            .interceptors(
                consumerIdClientInterceptor,
                bearerTokenWithSTSFallbackClientInterceptor,
                MdcValuesPropagatingClientInterceptor(),
            ).setConnectTimeout(Duration.ofSeconds(20))
            .setReadTimeout(Duration.ofSeconds(20))
            .build()

    @Bean("noAuthorize")
    fun restTemplateNoAuthorize(consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations =
        RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                MdcValuesPropagatingClientInterceptor(),
            ).setConnectTimeout(Duration.ofSeconds(20))
            .setReadTimeout(Duration.ofSeconds(20))
            .build()

    private fun RestTemplateBuilder.medProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplateBuilder =
        if (trengerProxy()) {
            this.additionalCustomizers(naisProxyCustomizer)
        } else {
            this
        }

    private fun trengerProxy(): Boolean =
        !environment.activeProfiles.any {
            listOf("e2e", "dev", "postgres", "integrasjonstest").contains(it.trim(' '))
        }
}
