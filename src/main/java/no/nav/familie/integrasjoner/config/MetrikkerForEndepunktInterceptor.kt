package no.nav.familie.integrasjoner.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor
import org.springframework.web.servlet.HandlerMapping

@Component
class MetrikkerForEndepunktInterceptor : AsyncHandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val path = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as String
        MetrikkerForEndepunkterInitialiserer.metrikkerForEndepunkter.get("[${request.method}]$path")?.increment()
        return super.preHandle(request, response, handler)
    }
}
