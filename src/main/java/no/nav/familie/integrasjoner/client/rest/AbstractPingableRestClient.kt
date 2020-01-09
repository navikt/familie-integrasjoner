package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.client.Pingable
import org.springframework.web.client.RestOperations
import org.springframework.web.client.getForEntity
import java.net.URI

abstract class AbstractPingableRestClient(operations: RestOperations,
                                          metricsPrefix: String) : AbstractRestClient(operations, metricsPrefix), Pingable {

    abstract val pingUri: URI

    override fun ping() {
        operations.getForEntity<String>(pingUri)
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}
