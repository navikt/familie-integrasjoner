package no.nav.familie.integrasjoner.felles

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.apache.commons.lang3.StringUtils

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}

fun graphqlQuery(path: String) = ApplicationConfig::class.java.getResource(path)
        .readText()
        .graphqlCompatible()
