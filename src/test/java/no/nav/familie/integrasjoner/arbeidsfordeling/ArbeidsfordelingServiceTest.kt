package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.personopplysning.internal.*
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.UGRADERT
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

internal class ArbeidsfordelingServiceTest {

    private val restClient: ArbeidsfordelingRestClient = mockk()
    private val pdlRestClient: PdlRestClient = mockk()
    private val egenAnsattService: EgenAnsattService = mockk()
    private val cacheManager = ConcurrentMapCacheManager()
    private val arbeidsfordelingService =
            ArbeidsfordelingService(klient = mockk(),
                                    restClient = restClient,
                                    pdlRestClient = pdlRestClient,
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
        mockPersonInfo(mapOf(ident to UGRADERT))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når søker er kode 6`() {
        mockPersonInfo(mapOf(ident to STRENGT_FORTROLIG))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når søker er kode 7`() {
        mockPersonInfo(mapOf(ident to FORTROLIG))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når søker er egen ansatt`() {
        mockPersonInfo(mapOf(ident to UGRADERT), egenAnsatte = setOf(ident))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når barn er kode 6`() {
        mockPersonInfo(mapOf(ident to UGRADERT), mapOf(barnXIdent to STRENGT_FORTROLIG))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når ektefelle er kode 6`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = ektefelleIdent to STRENGT_FORTROLIG)
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når annenForelder er kode 6`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = annenForelderZIdent to STRENGT_FORTROLIG)
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når barn er kode 7`() {
        mockPersonInfo(mapOf(ident to UGRADERT), mapOf(barnXIdent to FORTROLIG))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når ektefelle er kode 7`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = ektefelleIdent to FORTROLIG)
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med diskresjonskode når annenForelder er kode 7`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = annenForelderXIdent to FORTROLIG)
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", false) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når barn er egen ansatt`() {
        mockPersonInfo(mapOf(ident to UGRADERT), mapOf(barnXIdent to UGRADERT), egenAnsatte = setOf(barnXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når ektefelle er egen ansatt`() {
        mockPersonInfo(mapOf(ident to UGRADERT),  ektefelle = ektefelleIdent to UGRADERT, egenAnsatte = setOf(ektefelleIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når annenForelder er egen ansatt`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = annenForelderXIdent to UGRADERT, egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }


    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med egenAnsatt når annenForelder er egen ansatt og barn er kode7`() {
        mockPersonInfo(mapOf(ident to UGRADERT), mapOf(barnXIdent to FORTROLIG), annenForelderXIdent to UGRADERT, egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), null, true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med strengeste diskresjonskode når alle er representert`() {
        mockPersonInfo(mapOf(ident to UGRADERT), mapOf(barnXIdent to FORTROLIG), annenForelderXIdent to STRENGT_FORTROLIG, egenAnsatte = setOf(annenForelderXIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPSF", true) }
    }

    @Test
    fun `EnhetForPersonMedRelasjoner - skal hente med både diskresjonskode 7 og egenansatt når én person er dette`() {
        mockPersonInfo(mapOf(ident to UGRADERT), ektefelle = ektefelleIdent to FORTROLIG, egenAnsatte = setOf(ektefelleIdent))
        arbeidsfordelingService.finnBehandlendeEnhetForPersonMedRelasjoner(ident, Tema.ENF)

        verify { restClient.finnBehandlendeEnhetMedBesteMatch(any(), any(), "SPFO", true) }
    }

    private fun mockPersonInfo(
        Personer: Map<String, ADRESSEBESKYTTELSEGRADERING>,
        barna: Map<String, ADRESSEBESKYTTELSEGRADERING> = emptyMap(),
        ektefelle: Pair<String, ADRESSEBESKYTTELSEGRADERING> = ektefelleIdent to UGRADERT,
        egenAnsatte: Set<String> = emptySet()
    ) {
        for (person in Personer) {
            val hovedPerson = person.key to
                    lagPdlPersonMedRelasjoner(
                        familierelasjoner = barna.keys.map {
                            PdlForelderBarnRelasjon(
                                it,
                                FORELDERBARNRELASJONROLLE.BARN
                            )
                        }.toMutableList()
                            .also {
                                it.add(PdlForelderBarnRelasjon(ektefelle.first, FORELDERBARNRELASJONROLLE.FAR))
                            },
                        sivilstand = listOf(Sivilstand(SIVILSTAND.GIFT, "3")),
                        fullmakt = listOf(Fullmakt("4")),
                        adressebeskyttelse = listOf(Adressebeskyttelse(person.value))
                    )


            every { pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(person.key), any()) } returns mapOf(
                hovedPerson
            )

            every { pdlRestClient.hentAdressebeskyttelse(person.key, Tema.ENF) } returns PdlPersonMedAdressebeskyttelse(
                PdlAdressebeskyttelse(
                    listOf(
                        Adressebeskyttelse(person.value)
                    )
                )
            )

            for (barn in barna) {
                val barnPerson = barn.key to
                        lagPdlPersonMedRelasjoner(
                            familierelasjoner = listOf(
                                PdlForelderBarnRelasjon(
                                    person.key,
                                    FORELDERBARNRELASJONROLLE.MOR
                                )
                            ),
                            sivilstand = listOf(Sivilstand(SIVILSTAND.GIFT, "3")),
                            fullmakt = listOf(Fullmakt("4"))
                        )

                every { pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(barn.key), any()) } returns mapOf(
                    barnPerson
                )

                every { pdlRestClient.hentAdressebeskyttelse(barn.key, Tema.ENF) } returns PdlPersonMedAdressebeskyttelse(
                    PdlAdressebeskyttelse(
                        listOf(
                            Adressebeskyttelse(barn.value)
                        )
                    )
                )
            }

            val ektefellePerson = ektefelle.first to
                    lagPdlPersonMedRelasjoner(
                        familierelasjoner = listOf(
                            PdlForelderBarnRelasjon(
                                person.key,
                                FORELDERBARNRELASJONROLLE.MOR
                            )
                        ),
                        sivilstand = listOf(Sivilstand(SIVILSTAND.GIFT, "3")),
                        fullmakt = listOf(Fullmakt("4"))
                    )
            every { pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(ektefelle.first), any()) } returns mapOf(
                ektefellePerson
            )

            every { pdlRestClient.hentAdressebeskyttelse(ektefelle.first, Tema.ENF) } returns PdlPersonMedAdressebeskyttelse(
                PdlAdressebeskyttelse(
                    listOf(
                        Adressebeskyttelse(ektefelle.second)
                    )
                )
            )
        }
        every { egenAnsattService.erEgenAnsatt(any<Set<String>>()) } returns egenAnsatte.associateWith { true }

        every {
            pdlRestClient.hentGeografiskTilknytning(any(), any())
        } returns GeografiskTilknytningDto(
            gtType = GeografiskTilknytningType.KOMMUNE,
            gtBydel = null,
            gtKommune = "3032",
            gtLand = null
        )

    }

    private fun lagPdlPersonMedRelasjoner(familierelasjoner: List<PdlForelderBarnRelasjon> = emptyList(),
                                          sivilstand: List<Sivilstand> = emptyList(),
                                          fullmakt: List<Fullmakt> = emptyList(),
                                          adressebeskyttelse: List<Adressebeskyttelse> = emptyList()) =
        PdlPersonMedRelasjonerOgAdressebeskyttelse(familierelasjoner, sivilstand, fullmakt, adressebeskyttelse)

}