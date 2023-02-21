package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("no.nav.security.jwt.tilgang")
class TilgangConfig(val grupper: Map<String, AdRolle>)
