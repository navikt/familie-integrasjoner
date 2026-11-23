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
    fun `skal returnere status for kjente toggles og false for ukjent toggle`() {
        val unleashService = FakeUnleashService(mapOf("aktiv" to true, "deaktivert" to false))
        val service = FeatureToggleService(unleashService)

        assertThat(service.isEnabled("aktiv")).isTrue()
        assertThat(service.isEnabled("deaktivert")).isFalse()
        assertThat(service.isEnabled("ukjent-toggle")).isFalse()
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
                        assertThat(context.getBean(FeatureToggleService::class.java).isEnabled(toggleId)).isFalse()
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
        ): Boolean = toggles[toggleId] ?: defaultValue

        override fun destroy() = Unit
    }
}
