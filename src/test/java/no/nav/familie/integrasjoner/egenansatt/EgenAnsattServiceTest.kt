package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import io.mockk.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-egenansatt")
class EgenAnsattServiceTest : OppslagSpringRunnerTest() {

    @Autowired
    lateinit var egenAnsattSoapClient: EgenAnsattSoapClient

    @Autowired
    lateinit var egenAnsattService: EgenAnsattService

    @Test
    fun `skal cachea erEgenAnsatt`() {
        every { egenAnsattSoapClient.erEgenAnsatt(any()) } returns true
        repeat(3) {
            assertThat(egenAnsattService.erEgenAnsatt("1")).isTrue()
            egenAnsattService.erEgenAnsatt("2")
        }

        verify(exactly = 1) { egenAnsattSoapClient.erEgenAnsatt("1") }
        verify(exactly = 1) { egenAnsattSoapClient.erEgenAnsatt("2") }
    }
}