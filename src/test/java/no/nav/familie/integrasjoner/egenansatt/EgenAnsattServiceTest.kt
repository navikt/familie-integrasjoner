package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EgenAnsattServiceTest {
    private val egenAnsattRestClientMock: EgenAnsattRestClient = mockk()
    private val egenAnsattService = EgenAnsattService(egenAnsattRestClientMock)

    @Test
    fun `Er egen ansatt`() {
        every { egenAnsattRestClientMock.erEgenAnsatt(any<String>()) } returns true
        assertThat(egenAnsattService.erEgenAnsatt("1")).isTrue
    }
}