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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Configuration
@Import(ConsumerIdClientInterceptor::class,
        BearerTokenClientInterceptor::class,
        StsBearerTokenClientInterceptor::class,
        BearerTokenWithSTSFallbackClientInterceptor::class,
        BearerTokenClientCredentialsClientInterceptor::class)
class RestTemplateConfig(
        private val environment: Environment
) {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun restTemplateMedProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplate {
        return RestTemplateBuilder()
                .medProxy(naisProxyCustomizer)
                .build()
    }

    @Bean
    fun restTemplateBuilderMedProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplateBuilder {
        return RestTemplateBuilder()
                .medProxy(naisProxyCustomizer)
    }

    /**
     * Denne bruker jwt-bearer hvis den finnes, hvis ikke så bruker den client_credentials
     */
    @Bean("jwtBearer")
    fun restTemplateJwtBearer(naisProxyCustomizer: NaisProxyCustomizer,
                              consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              bearerTokenClientInterceptor: BearerTokenClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .medProxy(naisProxyCustomizer)
                .interceptors(consumerIdClientInterceptor,
                              bearerTokenClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .requestFactory(this::requestFactory)
                .build()
    }

    @Bean("clientCredential")
    fun restTemplateJwtBearer(naisProxyCustomizer: NaisProxyCustomizer,
                              consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                              bearerTokenClientInterceptor: BearerTokenClientCredentialsClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .medProxy(naisProxyCustomizer)
                .interceptors(consumerIdClientInterceptor,
                              bearerTokenClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .requestFactory(this::requestFactory)
                .build()
    }

    @Bean("jwtBearerOboOgSts")
    fun restTemplateOboOgSts(naisProxyCustomizer: NaisProxyCustomizer,
                             consumerIdClientInterceptor: ConsumerIdClientInterceptor,
                             bearerTokenWithSTSFallbackClientInterceptor: BearerTokenWithSTSFallbackClientInterceptor): RestOperations {
        return RestTemplateBuilder()
                .medProxy(naisProxyCustomizer)
                .interceptors(consumerIdClientInterceptor,
                              bearerTokenWithSTSFallbackClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .requestFactory(this::requestFactory)
                .build()
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

    @Bean("noAuthorize")
    fun restTemplateNoAuthorize(consumerIdClientInterceptor: ConsumerIdClientInterceptor): RestOperations {

        return RestTemplateBuilder()
                .interceptors(consumerIdClientInterceptor,
                              MdcValuesPropagatingClientInterceptor())
                .requestFactory(this::requestFactory)
                .build()
    }

    private fun RestTemplateBuilder.medProxy(naisProxyCustomizer: NaisProxyCustomizer): RestTemplateBuilder {
        return if (trengerProxy()) {
            this.additionalCustomizers(naisProxyCustomizer)
        } else {
            this
        }
    }

    private fun requestFactory() = HttpComponentsClientHttpRequestFactory()
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
}