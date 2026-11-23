package no.nav.familie.integrasjoner.config.featureToggle

import no.nav.familie.unleash.UnleashService
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    private val unleashService: UnleashService,
) {
    fun isEnabled(toggleId: String): Boolean = unleashService.isEnabled(toggleId)
}
