package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("tilgang")
@ConstructorBinding
class TilgangConfig(
    val kode6: AdRolle,
    val kode7: AdRolle,
    val egenAnsatt: AdRolle,
)
