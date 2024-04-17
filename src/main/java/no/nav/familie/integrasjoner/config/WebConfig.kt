package no.nav.familie.integrasjoner.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(MetrikkerForEndepunktInterceptor()).addPathPatterns("/api/**")
        super.addInterceptors(registry)
    }
}
