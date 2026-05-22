package no.nav.familie.integrasjoner.sikkerhet

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64

@Component
class TokenLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(TokenLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val tokenType = bestemTokenType(request)
        MDC.put("token_type", tokenType.name)
        log.info("Innkommende kall - tokentype={} path={} method={}", tokenType, request.requestURI, request.method)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("token_type")
        }
    }

    private fun bestemTokenType(request: HttpServletRequest): TokenType {
        val token =
            request
                .getHeader("Authorization")
                ?.removePrefix("Bearer ")
                ?: return TokenType.INGEN

        val claims = parseJwtPayload(token) ?: return TokenType.UKJENT
        val iss = claims["iss"] as? String ?: return TokenType.UKJENT

        return when {
            "login.microsoftonline.com" in iss -> {
                val oid = claims["oid"] as? String
                val sub = claims["sub"] as? String

                @Suppress("UNCHECKED_CAST")
                val roles = claims["roles"] as? List<String> ?: emptyList()
                val erM2M = oid != null && oid == sub && "access_as_application" in roles
                if (erM2M) TokenType.AZURE_AD_M2M else TokenType.AZURE_AD_OBO
            }

            "tokenx" in iss -> {
                TokenType.TOKEN_X
            }

            else -> {
                TokenType.UKJENT
            }
        }
    }

    private fun parseJwtPayload(token: String): Map<*, *>? =
        runCatching {
            val payload = token.split(".").getOrNull(1) ?: return null
            val json = Base64.getUrlDecoder().decode(payload)
            jacksonObjectMapper().readValue(json, Map::class.java)
        }.getOrNull()

    enum class TokenType {
        AZURE_AD_OBO,
        AZURE_AD_M2M,
        TOKEN_X,
        INGEN,
        UKJENT,
    }
}
