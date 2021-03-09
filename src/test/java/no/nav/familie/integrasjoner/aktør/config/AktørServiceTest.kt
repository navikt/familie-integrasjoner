package no.nav.familie.integrasjoner.aktør.config

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.aktør.domene.Aktør
import no.nav.familie.integrasjoner.aktør.domene.Ident
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.config.CacheConfig
import no.nav.familie.integrasjoner.felles.Tema
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@ActiveProfiles(profiles = ["AktørServiceTest"])
@RunWith(SpringRunner::class)
@EnableCaching
class AktørServiceTest {

    @Autowired private lateinit var aktørService: AktørService
    @Autowired private lateinit var aktørClient: AktørregisterRestClient
    @Autowired private lateinit var pdlRestClient: PdlRestClient
    @Autowired private lateinit var cacheManager: CacheManager


    @Configuration
    @Profile("AktørServiceTest")
    class AktørClientTestConfig {

        val cacheManagerToBe = CacheConfig().cacheManager()

        @Bean
        @Primary
        fun aktørregisterClientMock(): AktørregisterRestClient = mockk()

        @Bean
        @Primary
        fun pdlRestClient(): PdlRestClient = mockk()

        @Bean
        @Primary
        fun cacheManager(): CacheManager {
            return cacheManagerToBe
        }

        @Bean
        @Primary
        fun aktørService(): AktørService = AktørService(aktørregisterClientMock(), pdlRestClient())

    }

    @Before
    fun setUp() {
        every { aktørClient.hentAktørId(eq(PERSONIDENT)) } returns
                AktørResponse().withAktør(PERSONIDENT, Aktør()
                        .withIdenter(listOf(Ident().withIdent(TESTAKTORID).withGjeldende(true))))
        every { aktørClient.hentAktørId(eq(PERSONIDENT_UTEN_IDENT)) } returns
                AktørResponse().withAktør(PERSONIDENT_UTEN_IDENT, Aktør()
                        .withIdenter(listOf(Ident().withIdent(null).withGjeldende(true))))
        every { pdlRestClient.hentGjeldendeAktørId(eq(PERSONIDENT), eq(Tema.ENF.name)) } returns TESTAKTORID
        every { pdlRestClient.hentGjeldendePersonident(eq(TESTAKTORID), eq(Tema.ENF.name)) } returns PERSONIDENT
        every { pdlRestClient.hentGjeldendeAktørId(eq(PERSONIDENT_UTEN_IDENT), eq(Tema.ENF.name)) } throws PdlNotFoundException()
    }

    @After
    fun tearDown() {
        clearMocks(aktørClient)
        clearMocks(pdlRestClient)
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)!!.clear() }
    }

    @Test
    fun `skal returnere aktørId fra register første gang`() {
        val testAktørId = aktørService.getAktørId(PERSONIDENT)
        assertThat(testAktørId).isEqualTo(TESTAKTORID)
        verify(exactly = 1) { aktørClient.hentAktørId(any()) }
    }

    @Test
    fun `skal returnere aktørId fra pdl første gang`() {
        val testAktørId = aktørService.getAktørIdFraPdl(PERSONIDENT, Tema.ENF.name)
        assertThat(testAktørId).isEqualTo(TESTAKTORID)
        verify(exactly = 1) { (pdlRestClient.hentGjeldendeAktørId(any(), any())) }
    }

    @Test
    fun `skal returnere aktørId fra cache når den ligger den`() {
        repeat(2) {
            assertThat(aktørService.getAktørId(PERSONIDENT)).isEqualTo(TESTAKTORID)
        }
        verify(exactly = 1) { aktørClient.hentAktørId(any()) }
    }

    @Test
    fun `skal returnere aktørId fra cache når den ligger der fra pdl`() {
        repeat(2) {
            assertThat(aktørService.getAktørIdFraPdl(PERSONIDENT, Tema.ENF.name)).isEqualTo(TESTAKTORID)
        }
        verify(exactly = 1) { pdlRestClient.hentGjeldendeAktørId(any(), any()) }
    }

    @Test
    fun `skal ikke cachea nullverdier`() {
        repeat(2) {
            assertThat(aktørService.getAktørId(PERSONIDENT_UTEN_IDENT)).isNull()
        }
        verify(exactly = 2) { aktørClient.hentAktørId(any()) }
    }


    @Test
    fun `skal ikke cachea når den feiler`() {
        repeat(2) {
            assertThrows<Exception> {
                aktørService.getAktørIdFraPdl(PERSONIDENT_UTEN_IDENT, Tema.ENF.name)
            }
        }
        verify(exactly = 2) { pdlRestClient.hentGjeldendeAktørId(any(), any()) }
    }

    companion object {
        private const val PERSONIDENT = "11111111111"
        private const val PERSONIDENT_UTEN_IDENT = "22222222222"
        private const val TESTAKTORID = "1000011111111"
    }
}