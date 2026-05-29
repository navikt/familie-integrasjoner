package no.nav.familie.integrasjoner.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.jsonMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler

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
            exceptionHandling {
                accessDeniedHandler = accessDeniedHandler()
                authenticationEntryPoint = authenticationEntryPoint()
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
            exceptionHandling {
                accessDeniedHandler = accessDeniedHandler()
                authenticationEntryPoint = authenticationEntryPoint()
            }
        }
        return http.build()
    }

    private fun accessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { _: HttpServletRequest, response: HttpServletResponse, _: AccessDeniedException ->
            response.apply {
                status = HttpServletResponse.SC_FORBIDDEN
                contentType = MediaType.APPLICATION_JSON_VALUE
                characterEncoding = Charsets.UTF_8.name()
                jsonMapper.writeValue(
                    writer,
                    Ressurs(
                        data = null,
                        status = Ressurs.Status.IKKE_TILGANG,
                        melding = "Bruker har ikke tilgang",
                        frontendFeilmelding = "Du mangler tilgang",
                        stacktrace = null,
                    ),
                )
            }
        }

    private fun authenticationEntryPoint(): AuthenticationEntryPoint =
        AuthenticationEntryPoint { _: HttpServletRequest, response: HttpServletResponse, _: AuthenticationException ->
            response.apply {
                status = HttpServletResponse.SC_UNAUTHORIZED
                contentType = MediaType.APPLICATION_JSON_VALUE
                characterEncoding = Charsets.UTF_8.name()
                jsonMapper.writeValue(
                    writer,
                    failure<Nothing>(
                        errorMessage = "401 Unauthorized",
                        frontendFeilmelding = "Kall ikke autorisert",
                    ),
                )
            }
        }
}
