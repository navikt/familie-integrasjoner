package no.nav.familie.integrasjoner.aktør.config

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.aktør.domene.Aktør
import no.nav.familie.integrasjoner.aktør.domene.Ident
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(profiles = ["integrasjonstest", "mock-sts", "AktørServiceTest"])
class AktørServiceTest : OppslagSpringRunnerTest() {

    @Autowired private lateinit var aktørService: AktørService
    @Autowired private lateinit var aktørClient: AktørregisterRestClient
    @Autowired private lateinit var cacheManager: CacheManager

    @Configuration
    @Profile("AktørServiceTest")
    class AktørClientTestConfig {

        @Bean
        @Primary
        fun aktørregisterClientMock(): AktørregisterRestClient = mockk()
    }

    @Before
    fun setUp() {
        every { aktørClient.hentAktørId(eq(PERSONIDENT)) } returns
                AktørResponse().withAktør(PERSONIDENT, Aktør()
                        .withIdenter(listOf(Ident().withIdent(TESTAKTORID).withGjeldende(true))))
        every { aktørClient.hentAktørId(eq(PERSONIDENT_UTEN_IDENT)) } returns
                AktørResponse().withAktør(PERSONIDENT_UTEN_IDENT, Aktør()
                        .withIdenter(listOf(Ident().withIdent(null).withGjeldende(true))))
    }

    @After
    fun tearDown() {
        clearMocks(aktørClient)
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)!!.clear() }
    }

    @Test
    fun `skal returnere aktørId fra register første gang`() {
        val testAktørId = aktørService.getAktørId(PERSONIDENT)
        assertThat(testAktørId).isEqualTo(TESTAKTORID)
        verify(exactly = 1) { aktørClient.hentAktørId(any()) }
    }

    @Test
    fun `skal returnere aktørId fra cache når den ligger den`() {
        repeat(2) {
            assertThat(aktørService.getAktørId(PERSONIDENT)).isEqualTo(TESTAKTORID)
        }
        verify(exactly = 1) { aktørClient.hentAktørId(any()) }
    }

    @Test
    fun `skal ikke cachea nullverdier`() {
        repeat(2) {
            assertThat(aktørService.getAktørId(PERSONIDENT_UTEN_IDENT)).isNull()
        }
        verify(exactly = 2) { aktørClient.hentAktørId(any()) }
    }

    companion object {
        private const val PERSONIDENT = "11111111111"
        private const val PERSONIDENT_UTEN_IDENT = "22222222222"
        private const val TESTAKTORID = "1000011111111"
    }
}