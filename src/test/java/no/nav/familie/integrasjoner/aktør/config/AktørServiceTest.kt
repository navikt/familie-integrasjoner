package no.nav.familie.integrasjoner.aktør.config

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.aktør.AktørService
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.config.CacheConfig
import no.nav.familie.kontrakter.felles.Tema
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
        every { pdlRestClient.hentGjeldendePersonident(eq(TESTAKTORID), eq(Tema.ENF)) } returns PERSONIDENT
    }

    @AfterEach
    fun tearDown() {
        clearMocks(pdlRestClient)
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)!!.clear() }
    }

    companion object {
        private const val PERSONIDENT = "11111111111"
        private const val PERSONIDENT_UTEN_IDENT = "22222222222"
        private const val TESTAKTORID = "1000011111111"
    }
}
