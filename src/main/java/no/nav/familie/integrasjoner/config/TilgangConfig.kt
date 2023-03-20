package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("tilgang")
class TilgangConfig(
    val kode6: AdRolle,
    val kode7: AdRolle,
    val egenAnsatt: AdRolle,
)
