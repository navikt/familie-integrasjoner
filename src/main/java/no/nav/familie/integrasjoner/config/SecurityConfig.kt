package no.nav.familie.integrasjoner.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val azureAdAuthenticationManager: AzureAdAuthenticationManager,
    private val tokenXAuthenticationManager: TokenXAuthenticationManager,
) {
    @Bean
    @Order(1)
    fun publicFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher(
                "/internal/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/api/kodeverk/**",
                "/api/ping",
                "/favicon.ico",
            )
            csrf { disable() }
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
        }
        return http.build()
    }

    @Bean
    @Order(2)
    fun tokenXFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/api/journalpostselvbetjening/**")
            csrf { disable() }
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    authenticationManager = tokenXAuthenticationManager
                }
            }
        }
        return http.build()
    }

    @Bean
    @Order(3)
    fun azureAdFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer {
                jwt {
                    authenticationManager = azureAdAuthenticationManager
                }
            }
        }
        return http.build()
    }
}
