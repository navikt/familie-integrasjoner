package no.nav.familie.integrasjoner.felles

import org.apache.commons.lang3.StringUtils

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}