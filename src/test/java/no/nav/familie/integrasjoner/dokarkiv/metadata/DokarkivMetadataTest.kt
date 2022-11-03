package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockserver.junit.jupiter.MockServerExtension
import org.mockserver.junit.jupiter.MockServerSettings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "mock-kodeverk"])
@ExtendWith(MockServerExtension::class)
@MockServerSettings(ports = [OppslagSpringRunnerTest.MOCK_SERVER_PORT])
internal class DokarkivMetadataTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var dokarkivMetadata: DokarkivMetadata

    @Test
    internal fun `brevkode må være under 50 tegn`() {
        val tittelOver50Tegn = dokarkivMetadata.metadata.values.filter { (it.brevkode?.length ?: 0) > 50 }
        assertThat(tittelOver50Tegn).isEmpty()
    }
}