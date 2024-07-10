package no.nav.familie.integrasjoner.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties
data class KodeverkConfig(
    val KODEVERK_URL: String,
)
