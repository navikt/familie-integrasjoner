package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.UGRADERT
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

internal class ArbeidsfordelingServiceTest {

    private val restClient: ArbeidsfordelingRestClient = mockk()
    private val pdlRestClient: PdlRestClient = mockk()
    private val personopplysningerService: PersonopplysningerService = mockk()
    private val egenAnsattService: EgenAnsattService = mockk()
    private val cacheManager = ConcurrentMapCacheManager()
    private val arbeidsfordelingService =
            ArbeidsfordelingService(klient = mockk(),
                                    restClient = restClient,
                                    pdlRestClient = pdlRestClient,
                                    personopplysningerService = personopplysningerService,
                                    egenAnsattService = egenAnsattService,
                                    cacheManager = cacheManager)

    val ident = "12345678901"
    val ektefelleIdent = "11111111111"
    val barnXIdent = "22222222222"
    val annenForelderXIdent = "22222222221"
    val barnZIdent = "33333333333"
    val annenForelderZIdent = "33333333331"
    val fullmaktIdent = "99999999999"

    @BeforeEach
    internal fun setUp() {
        every {
            restClient.finnBehandlendeEnhetMedBesteMatch(eq(Tema.ENF),
                                                         any(),
                                                         eq("SPSF"),
                                                         any())
        } returns listOf(Enhet("2103", "NAV Vikafossen"))

        every {
            restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), any(), any())
        } returns listOf(Enhet("1234", "En enhet"))
    }

    @Test
    fun `skal returnere null når geografisk tilknytning ikke definert`() {

        every {
            pdlRestClient.hentGeografiskTilknytning(ident,
                                                    any())
        } returns GeografiskTilknytningDto(GeografiskTilknytningType.UDEFINERT, null, null, "Land")

        assertThat(arbeidsfordelingService.finnLokaltNavKontor(ident, Tema.ENF)).isNull()
    }

    @Test
    fun `skal utlede riktig geografisk tilknytning kode`() {

        every {
            pdlRestClient.hentGeografiskTilknytning(ident,
                                                    any())
        } returns GeografiskTilknytningDto(gtType = GeografiskTilknytningType.KOMMUNE,
                                           gtKommune = "2372",
                                           gtBydel = null,
                                           gtLand = null)

        every { restClient.hentEnhet(any()) } returns mockk()

        arbeidsfordelingService.finnLokaltNavKontor(ident, Tema.ENF)

        verify { restClient.hentEnhet("2372") }
    }


    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente uten diskresjonskode eller egenansatt når ingen er dette`() {
        mockPersonInfo(emptySet(), emptySet(), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når søker er kode 6`() {
        mockPersonInfo(kode6 = setOf(ident), emptySet(), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når søker er kode 7`() {
        mockPersonInfo(emptySet(), kode7 = setOf(ident), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når søker er egen ansatt`() {
        mockPersonInfo(emptySet(), emptySet(), egenAnsatte = setOf(ident))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når barn er kode 6`() {
        mockPersonInfo(kode6 = setOf(barnXIdent), emptySet(), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når ektefelle er kode 6`() {
        mockPersonInfo(kode6 = setOf(ektefelleIdent), emptySet(), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når annenForelder er kode 6`() {
        mockPersonInfo(kode6 = setOf(annenForelderZIdent), emptySet(), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når barn er kode 7`() {
        mockPersonInfo(emptySet(), kode7 = setOf(barnXIdent), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når ektefelle er kode 7`() {
        mockPersonInfo(emptySet(), kode7 = setOf(ektefelleIdent), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når annenForelder er kode 7`() {
        mockPersonInfo(emptySet(), kode7 = setOf(annenForelderXIdent), emptySet())
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når barn er egen ansatt`() {
        mockPersonInfo(emptySet(), emptySet(), egenAnsatte = setOf(barnXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når ektefelle er egen ansatt`() {
        mockPersonInfo(emptySet(), emptySet(), egenAnsatte = setOf(ektefelleIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når annenForelder er egen ansatt`() {
        mockPersonInfo(emptySet(), emptySet(), egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }


    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når annenForelder er egen ansatt og barn er kode7`() {
        mockPersonInfo(emptySet(), kode7 = setOf(barnXIdent), egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med strengeste diskresjonskode når alle er representert`() {
        mockPersonInfo(kode6 = setOf(ektefelleIdent), kode7 = setOf(barnXIdent), egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med både diskresjonskode 7 og egenansatt når én person er dette`() {
        mockPersonInfo(emptySet(), kode7 = setOf(ektefelleIdent, annenForelderXIdent), egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", true) }
    }

    private fun mockPersonInfo(kode6: Set<String>, kode7: Set<String>, egenAnsatte: Set<String>) {
        val ektefelle = PersonMedAdresseBeskyttelse(ektefelleIdent, utledAdressebeskyttelse(ektefelleIdent, kode6, kode7))
        val fullmakt = PersonMedAdresseBeskyttelse(fullmaktIdent, utledAdressebeskyttelse(fullmaktIdent, kode6, kode7))
        val barnX = PersonMedAdresseBeskyttelse(barnXIdent, utledAdressebeskyttelse(barnXIdent, kode6, kode7))
        val barnZ = PersonMedAdresseBeskyttelse(barnZIdent, utledAdressebeskyttelse(barnZIdent, kode6, kode7))
        val annenForelderX =
                PersonMedAdresseBeskyttelse(annenForelderXIdent, utledAdressebeskyttelse(annenForelderXIdent, kode6, kode7))
        val annenForelderZ =
                PersonMedAdresseBeskyttelse(annenForelderZIdent, utledAdressebeskyttelse(annenForelderZIdent, kode6, kode7))
        val relasjonerUtenGradering = PersonMedRelasjoner(
                personIdent = ident,
                adressebeskyttelse = utledAdressebeskyttelse(ident, kode6, kode7),
                sivilstand = listOf(ektefelle),
                fullmakt = listOf(fullmakt),
                barn = listOf(barnX, barnZ),
                barnsForeldrer = listOf(annenForelderX, annenForelderZ),
        )
        every {
            personopplysningerService.hentPersonMedRelasjoner(ident, any())
        } returns relasjonerUtenGradering

        every { egenAnsattService.erEgenAnsatt(any<Set<String>>()) } returns egenAnsatte.associateWith { true }

        every {
            pdlRestClient.hentGeografiskTilknytning(any(), any())
        } returns GeografiskTilknytningDto(gtType = GeografiskTilknytningType.KOMMUNE,
                                           gtBydel = null,
                                           gtKommune = "3032",
                                           gtLand = null
        )
    }

    fun utledAdressebeskyttelse(personIdent: String, kode6: Set<String>, kode7: Set<String>): ADRESSEBESKYTTELSEGRADERING? {
        return when {
            kode6.contains(personIdent) -> STRENGT_FORTROLIG
            kode7.contains(personIdent) -> FORTROLIG
            else -> UGRADERT
        }
    }

}