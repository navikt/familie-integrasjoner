package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.config.TilgangConfig
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.UGRADERT
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.integrasjoner.tilgangskontroll.domene.AdRolle
import no.nav.familie.kontrakter.felles.Tema
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        CachedTilgangskontrollServiceCacheTest.CacheTestConfig::class,
        TilgangskontrollCacheConfig::class,
    ],
)
internal class CachedTilgangskontrollServiceCacheTest {
    @Autowired
    lateinit var cachedTilgangskontrollService: CachedTilgangskontrollService

    @Autowired
    lateinit var cacheManager: CacheManager

    private val egenAnsattService = CacheTestConfig.egenAnsattService
    private val personopplysningerService = CacheTestConfig.personopplysningerService

    @BeforeEach
    fun setUp() {
        clearMocks(egenAnsattService, personopplysningerService)
        every { egenAnsattService.erEgenAnsatt(any<String>()) } returns false
        every { egenAnsattService.erEgenAnsatt(any<Set<String>>()) } answers { firstArg<Set<String>>().associateWith { false } }
        every { personopplysningerService.hentAdressebeskyttelse(any(), any()) } returns Adressebeskyttelse(UGRADERT)
        every { personopplysningerService.hentPersonMedRelasjoner(any(), any()) } returns lagPersonMedRelasjoner()

        mockToken("saksbehandler-1")

        cacheManager.resetCaches()
    }

    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sjekkTilgang - andre kall med samme personIdent og saksbehandler skal hentes fra cache`() {
        cachedTilgangskontrollService.sjekkTilgang(PERSON_IDENT, Tema.BAR)
        cachedTilgangskontrollService.sjekkTilgang(PERSON_IDENT, Tema.BAR)

        verify(exactly = 1) { personopplysningerService.hentAdressebeskyttelse(PERSON_IDENT, any()) }
    }

    @Test
    fun `sjekkTilgang - kall med ulik personIdent skal ikke treffe cache`() {
        cachedTilgangskontrollService.sjekkTilgang(PERSON_IDENT, Tema.BAR)
        cachedTilgangskontrollService.sjekkTilgang(ANNEN_PERSON_IDENT, Tema.BAR)

        verify(exactly = 1) { personopplysningerService.hentAdressebeskyttelse(PERSON_IDENT, any()) }
        verify(exactly = 1) { personopplysningerService.hentAdressebeskyttelse(ANNEN_PERSON_IDENT, any()) }
    }

    @Test
    fun `sjekkTilgang - kall med ulik saksbehandler skal ikke treffe cache`() {
        cachedTilgangskontrollService.sjekkTilgang(PERSON_IDENT, Tema.BAR)
        mockToken("saksbehandler-2")
        cachedTilgangskontrollService.sjekkTilgang(PERSON_IDENT, Tema.BAR)

        verify(exactly = 2) { personopplysningerService.hentAdressebeskyttelse(PERSON_IDENT, any()) }
    }

    @Test
    fun `sjekkTilgangTilPersonMedRelasjoner - andre kall med samme personIdent og saksbehandler skal hentes fra cache`() {
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(PERSON_IDENT, Tema.ENF)
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(PERSON_IDENT, Tema.ENF)

        verify(exactly = 1) { personopplysningerService.hentPersonMedRelasjoner(PERSON_IDENT, any()) }
    }

    @Test
    fun `sjekkTilgangTilPersonMedRelasjoner - kall med ulik personIdent skal ikke treffe cache`() {
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(PERSON_IDENT, Tema.ENF)
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(ANNEN_PERSON_IDENT, Tema.ENF)

        verify(exactly = 1) { personopplysningerService.hentPersonMedRelasjoner(PERSON_IDENT, any()) }
        verify(exactly = 1) { personopplysningerService.hentPersonMedRelasjoner(ANNEN_PERSON_IDENT, any()) }
    }

    @Test
    fun `sjekkTilgangTilPersonMedRelasjoner - kall med ulik saksbehandler skal ikke treffe cache`() {
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(PERSON_IDENT, Tema.ENF)
        mockToken("saksbehandler-2")
        cachedTilgangskontrollService.sjekkTilgangTilPersonMedRelasjoner(PERSON_IDENT, Tema.ENF)

        verify(exactly = 2) { personopplysningerService.hentPersonMedRelasjoner(PERSON_IDENT, any()) }
    }

    private fun mockToken(subject: String) {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("header", "header value")
                .subject(subject)
                .claim("groups", emptyList<String>())
                .build()
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.setContext(securityContext)
    }

    private fun lagPersonMedRelasjoner(): PersonMedRelasjoner =
        PersonMedRelasjoner(
            personIdent = PERSON_IDENT,
            adressebeskyttelse = null,
            sivilstand = emptyList(),
            barn = emptyList(),
            barnsForeldrer = emptyList(),
        )

    @Configuration
    @EnableCaching
    class CacheTestConfig {
        @Bean
        fun cacheManager(): CacheManager = ConcurrentMapCacheManager()

        @Bean
        fun cachedTilgangskontrollService() =
            CachedTilgangskontrollService(
                egenAnsattService,
                personopplysningerService,
                tilgangConfig,
            )

        companion object {
            val egenAnsattService: EgenAnsattService = mockk()
            val personopplysningerService: PersonopplysningerService = mockk()
            private val tilgangConfig =
                TilgangConfig(
                    kode6 = AdRolle("kode6-gruppe", "Strengt fortrolig adresse"),
                    kode7 = AdRolle("kode7-gruppe", "Fortrolig adresse"),
                    egenAnsatt = AdRolle("egenansatt-gruppe", "Egen ansatt"),
                )
        }
    }

    companion object {
        private const val PERSON_IDENT = "12345678901"
        private const val ANNEN_PERSON_IDENT = "99999999999"
    }
}
