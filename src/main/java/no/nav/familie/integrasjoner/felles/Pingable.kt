package no.nav.familie.integrasjoner.felles

import java.net.URI

/**
 * Interface for clients that support health-check pinging.
 * Replaces no.nav.familie.restklient.client.Pingable.
 */
interface Pingable {
    val pingUri: URI

    fun ping(): String
}
