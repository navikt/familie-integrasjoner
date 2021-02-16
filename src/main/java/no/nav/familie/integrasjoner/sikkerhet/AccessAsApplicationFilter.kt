package no.nav.familie.integrasjoner.sikkerhet

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(0)
class AccessAsApplicationFilter : OncePerRequestFilter() {


    /*
      Rolletildelingen skjer automatisk når man setter opp kallende applikasjoner som pre-authorized applikasjoner.
      roles (en liste/array) inneholder access_as_application
      Dette claimet er ikke til stede dersom tokenet inneholder brukerkontekst, dvs. skaffet via on-behalf-of-flyten.
      Om applikasjonen din forventer å motta begge typer tokens så bør det gjøres en sjekk på om tokenet inneholder brukerkontekst eller ikke først
     */

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        when (hasAccessAsApplication()) {
            true -> filterChain.doFilter(request, response)
            false -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authenticated, but unauthorized application")
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.substring(request.contextPath.length)
        return path.startsWith("/internal/")
                || path.startsWith("/swagger-ui/")
                || path == "/tables"
                || path.startsWith("/swagger-resources")
                || path.startsWith("/v2/api-docs")
    }

    private fun hasAccessAsApplication(): Boolean {
        return try {
            val claims = SpringTokenValidationContextHolder().tokenValidationContext.getClaims("azuread")

            //Dersom sub er lik oid så er tokenet anskaffet via client credentials flow, altså et token uten brukerinnvolvering. Om ikke, så er tokenet anskaffet på vegne av en bruker der oid er brukerens globale ID i Azure AD.
            if (claims.get("sub").equals(claims.get("oid"))) {
                val allowAccessAsApplication = (claims.get("roles") as List<String>? ?: emptyList()).contains("access_as_application")

                if (!allowAccessAsApplication) {
                    logger.error("Ugyldig systemtoken: ${claims.get("sub")}, ${claims.get("oid")}, ${claims.get("roles")}")
                }
                allowAccessAsApplication
            }
            else {
                true
            }
        } catch (e: Exception) {
            logger.error("Feilet med å hente azp fra token")
            false
        }
    }

}
