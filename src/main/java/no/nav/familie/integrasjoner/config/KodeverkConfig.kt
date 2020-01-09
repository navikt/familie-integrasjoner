package no.nav.familie.integrasjoner.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties
@ConstructorBinding
data class KodeverkConfig(val KODEVERK_URL: String) 
