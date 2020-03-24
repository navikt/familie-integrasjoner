package no.nav.familie.integrasjoner.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig(
        private val environment: Environment
) {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun restTemplateMedProxy(): RestTemplate {
        return if (trengerProxy()) {
            RestTemplateBuilder()
                    .additionalCustomizers(NaisProxyCustomizer())
                    .build()
        } else {
            RestTemplateBuilder()
                    .build()
        }
    }

    @Bean
    fun restTemplateBuilderMedProxy(): RestTemplateBuilder {
        return if (trengerProxy()) {
            RestTemplateBuilder()
                    .additionalCustomizers(NaisProxyCustomizer())
        } else {
            RestTemplateBuilder()
        }
    }

    private fun trengerProxy(): Boolean {
        return !environment.activeProfiles.any { listOf("e2e", "dev").contains(it.trim(' ')) }
    }
}