package no.nav.familie.integrasjoner.config

import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.restklient.sts.StsBearerTokenClientInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import java.time.Duration

/**
 * RestTemplate-konfig beholdt for SkyggesakRestClient (STS) og GraphQLWebClientConfig (TokenX).
 * Fjernes når disse klientene migreres/fjernes.
 */
@Configuration
class RestTemplateConfig {
    @Bean("sts")
    fun restTemplateSts(
        stsBearerTokenClientInterceptor: StsBearerTokenClientInterceptor,
        consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    ): RestOperations =
        RestTemplateBuilder()
            .interceptors(
                consumerIdClientInterceptor,
                stsBearerTokenClientInterceptor,
                MdcValuesPropagatingClientInterceptor(),
            ).connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(20))
            .build()
}
