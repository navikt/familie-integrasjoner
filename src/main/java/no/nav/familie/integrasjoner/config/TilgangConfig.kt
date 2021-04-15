package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("no.nav.security.jwt.tilgang")
@ConstructorBinding
class TilgangConfig(val grupper: Map<String, AdRolle>)
