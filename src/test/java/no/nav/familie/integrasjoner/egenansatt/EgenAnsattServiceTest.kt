package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import io.mockk.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("mock-sts", "mock-egenansatt")
class EgenAnsattServiceTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var egenAnsattSoapClient: EgenAnsattSoapClient
    @Autowired
    lateinit var egenAnsattService: EgenAnsattService

    @Test
    fun `skal cachea erEgenAnsatt`() {
        every { egenAnsattSoapClient.erEgenAnsatt(any()) } returns true
        repeat(3) {
            val erEgenAnsatt = egenAnsattService.erEgenAnsatt("1")
            assertThat(erEgenAnsatt).isTrue()
        }

        verify(exactly = 1) { egenAnsattSoapClient.erEgenAnsatt(any()) }
    }
}