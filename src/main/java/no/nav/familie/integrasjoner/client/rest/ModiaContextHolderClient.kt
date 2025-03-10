package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderRequest
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class ModiaContextHolderClient(
    @Value("\${MODIA_CONTEXT_HOLDER_URL}") private val baseURI: URI,
    @Qualifier("jwtBearer") restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "modia-context-holder") {
    private val contextUri = URI("$baseURI/api/context")

    fun hentContext(): ModiaContextHolderResponse =
        try {
            getForEntity(contextUri)
        } catch (e: Exception) {
            incrementLoggFeil("modia-context-holder")
            throw e
        }

    fun settContext(request: ModiaContextHolderRequest): ModiaContextHolderResponse =
        try {
            postForEntity(contextUri, request)
        } catch (e: Exception) {
            incrementLoggFeil("modia-context-holder")
            throw e
        }
}
