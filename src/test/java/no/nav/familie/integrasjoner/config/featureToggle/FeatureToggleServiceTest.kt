package no.nav.familie.integrasjoner.config.featureToggle

import no.nav.familie.unleash.UnleashConfig
import no.nav.familie.unleash.UnleashService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.core.env.PropertySource
import org.springframework.core.io.ClassPathResource
import java.util.UUID

class FeatureToggleServiceTest {
    @Test
    fun `skal returnere true for aktiv toggle med eksakt Unleash ID`() {
        val unleashService = FakeUnleashService(mapOf(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID.navn to true))
        val service = FeatureToggleService(unleashService)

        assertThat(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID.navn)
            .isEqualTo("familie-integrasjoner.saksbehandler-utelat-azureid")
        assertThat(service.isEnabled(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID)).isTrue()
        assertThat(unleashService.toggleIds).containsExactly("familie-integrasjoner.saksbehandler-utelat-azureid")
        assertThat(unleashService.defaultValues).containsExactly(false)
        assertThat(unleashService.propertiesMethodCalled).isFalse()
    }

    @Test
    fun `skal returnere false for deaktivert toggle`() {
        val unleashService = FakeUnleashService(mapOf(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID.navn to false))
        val service = FeatureToggleService(unleashService)

        assertThat(service.isEnabled(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID)).isFalse()
        assertThat(unleashService.toggleIds).containsExactly("familie-integrasjoner.saksbehandler-utelat-azureid")
        assertThat(unleashService.defaultValues).containsExactly(false)
        assertThat(unleashService.propertiesMethodCalled).isFalse()
    }

    @Test
    fun `skal returnere false når toggle mangler`() {
        val unleashService = FakeUnleashService(emptyMap())
        val service = FeatureToggleService(unleashService)

        assertThat(service.isEnabled(FeatureToggle.SAKSBEHANDLER_UTELAT_AZUREID)).isFalse()
        assertThat(unleashService.toggleIds).containsExactly("familie-integrasjoner.saksbehandler-utelat-azureid")
        assertThat(unleashService.defaultValues).containsExactly(false)
        assertThat(unleashService.propertiesMethodCalled).isFalse()
    }

    @Test
    fun `skal laste profiler med deaktivert unleash og starte dummy tjenestene`() {
        listOf("application-dev.yaml", "application-integrasjonstest.yaml", "application-e2e.yml")
            .forEach { profileFile ->
                val propertySource = loadProfile(profileFile)
                assertThat(propertySource.getProperty("unleash.enabled")).isEqualTo(false)
                assertThat(
                    listOf("UNLEASH_SERVER_API_URL", "UNLEASH_SERVER_API_TOKEN", "NAIS_APP_NAME")
                        .all { property ->
                            (propertySource.getProperty(property) as? String)?.isNotBlank() == true
                        },
                ).isTrue()

                ApplicationContextRunner()
                    .withUserConfiguration(UnleashConfig::class.java, FeatureToggleService::class.java)
                    .withInitializer { context -> context.environment.propertySources.addFirst(propertySource) }
                    .run { context ->
                        assertThat(context).hasNotFailed()
                        assertThat(context).hasSingleBean(UnleashService::class.java)
                        assertThat(context).hasSingleBean(FeatureToggleService::class.java)

                        val toggleId = "context-test-${UUID.randomUUID()}"
                        assertThat(context.getBean(UnleashService::class.java).isEnabled(toggleId)).isFalse()
                        assertThat(context.getBean(UnleashService::class.java).javaClass.name)
                            .doesNotContain("DefaultUnleashService")
                    }
            }
    }

    private fun loadProfile(profileFile: String): PropertySource<*> =
        YamlPropertySourceLoader()
            .load(profileFile, ClassPathResource(profileFile))
            .first()

    private class FakeUnleashService(
        private val toggles: Map<String, Boolean>,
    ) : UnleashService {
        val toggleIds = mutableListOf<String>()
        val defaultValues = mutableListOf<Boolean>()

        var propertiesMethodCalled = false
            private set

        override fun isEnabled(
            toggleId: String,
            properties: Map<String, String>,
        ): Boolean {
            propertiesMethodCalled = true
            error("Kontekst-properties skal ikke brukes")
        }

        override fun isEnabled(
            toggleId: String,
            defaultValue: Boolean,
        ): Boolean {
            toggleIds.add(toggleId)
            defaultValues.add(defaultValue)
            return toggles[toggleId] ?: defaultValue
        }

        override fun destroy() = Unit
    }
}
