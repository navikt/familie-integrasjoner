package no.nav.familie.integrasjoner.aktør.config

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.config.CacheConfig
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.kontrakter.felles.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles(profiles = ["AktørServiceTest"])
@ExtendWith(SpringExtension::class)
@EnableCaching
class AktørServiceTest {
    @Autowired
    private lateinit var aktørService: AktørService

    @Autowired
    private lateinit var pdlRestClient: PdlRestClient

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Configuration
    @Profile("AktørServiceTest")
    class AktørClientTestConfig {
        val cacheManagerToBe = CacheConfig().cacheManager()

        @Bean
        @Primary
        fun pdlRestClient(): PdlRestClient = mockk()

        @Bean
        @Primary
        fun cacheManager(): CacheManager = cacheManagerToBe

        @Bean
        @Primary
        fun aktørService(): AktørService = AktørService(pdlRestClient())
    }

    @BeforeEach
    fun setUp() {
        every { pdlRestClient.hentGjeldendeAktørId(eq(PERSONIDENT), eq(Tema.ENF)) } returns TESTAKTORID
        every { pdlRestClient.hentGjeldendePersonident(eq(TESTAKTORID), eq(Tema.ENF)) } returns PERSONIDENT
        every { pdlRestClient.hentGjeldendeAktørId(eq(PERSONIDENT_UTEN_IDENT), eq(Tema.ENF)) } throws PdlNotFoundException()
    }

    @AfterEach
    fun tearDown() {
        clearMocks(pdlRestClient)
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)!!.clear() }
    }

    @Test
    fun `skal returnere aktørId fra pdl første gang`() {
        val testAktørId = aktørService.getAktørIdFraPdl(PERSONIDENT, Tema.ENF)
        assertThat(testAktørId).isEqualTo(TESTAKTORID)
        verify(exactly = 1) { (pdlRestClient.hentGjeldendeAktørId(any(), any())) }
    }

    @Test
    fun `skal returnere aktørId fra cache når den ligger der fra pdl`() {
        repeat(2) {
            assertThat(aktørService.getAktørIdFraPdl(PERSONIDENT, Tema.ENF)).isEqualTo(TESTAKTORID)
        }
        verify(exactly = 1) { pdlRestClient.hentGjeldendeAktørId(any(), any()) }
    }

    @Test
    fun `skal ikke cachea når den feiler`() {
        repeat(2) {
            assertThrows<Exception> {
                aktørService.getAktørIdFraPdl(PERSONIDENT_UTEN_IDENT, Tema.ENF)
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
