package no.nav.familie.integrasjoner.felles

import java.net.URI

object UriUtil {
    fun uri(
        base: URI,
        path: String,
    ): URI {
        val baseStr = base.toString().trimEnd('/')
        val pathStr = path.trimStart('/')
        return URI.create("$baseStr/$pathStr")
    }

    fun uri(
        base: URI,
        path: String,
        query: String,
    ): URI {
        val baseStr = base.toString().trimEnd('/')
        val pathStr = path.trimStart('/')
        return URI.create("$baseStr/$pathStr?$query")
    }
}
