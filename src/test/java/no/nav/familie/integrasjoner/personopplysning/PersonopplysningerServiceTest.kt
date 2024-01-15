package no.nav.familie.integrasjoner.personopplysning

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.integrasjoner.client.rest.PdlClientCredentialRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.client.rest.RegoppslagRestClient
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.FORELDERBARNRELASJONROLLE
import no.nav.familie.integrasjoner.personopplysning.internal.Fullmakt
import no.nav.familie.integrasjoner.personopplysning.internal.PdlForelderBarnRelasjon
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Sivilstand
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonopplysningerServiceTest {

    private lateinit var personopplysningerService: PersonopplysningerService

    private val pdlRestClient = mockk<PdlRestClient>()
    private val pdlClientCredentialRestClient = mockk<PdlClientCredentialRestClient>()
    private val regoppslagRestClient = mockk<RegoppslagRestClient>()

    @BeforeEach
    fun setUp() {
        personopplysningerService = PersonopplysningerService(
            pdlRestClient,
            pdlClientCredentialRestClient,
            regoppslagRestClient,
        )
    }

    @Test
    fun `hentPersonMedRelasjoner skal kalle på pdl 3 ganger, hovedpersonen, relasjonene og barnets andre forelder`() {
        val hovedPerson = "1" to
            lagPdlPersonMedRelasjoner(
                familierelasjoner = listOf(
                    PdlForelderBarnRelasjon(
                        "2",
                        FORELDERBARNRELASJONROLLE.BARN,
                    ),
                ),
                sivilstand = listOf(Sivilstand(SIVILSTAND.GIFT, "3")),
                fullmakt = listOf(Fullmakt("4")),
            )
        val barn = lagPdlPersonMedRelasjoner(
            familierelasjoner = listOf(
                PdlForelderBarnRelasjon(
                    "22",
                    FORELDERBARNRELASJONROLLE.FAR,
                ),
            ),
        )
        every { pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(any(), any()) } answers {
            firstArg<List<String>>().map { it to if (it == "2") barn else lagPdlPersonMedRelasjoner() }.toMap()
        }
        every { pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("1"), any()) } returns mapOf(hovedPerson)

        val hentPersonMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner("1", Tema.ENF)

        assertThat(hentPersonMedRelasjoner.barn.single().personIdent).isEqualTo("2")
        assertThat(hentPersonMedRelasjoner.barnsForeldrer.single().personIdent).isEqualTo("22")
        assertThat(hentPersonMedRelasjoner.sivilstand.single().personIdent).isEqualTo("3")
        assertThat(hentPersonMedRelasjoner.fullmakt.single().personIdent).isEqualTo("4")

        verify(exactly = 1) {
            pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("1"), any())
            pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("2", "3", "4"), any())
            pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf("22"), any())
        }
    }

    private fun lagPdlPersonMedRelasjoner(
        familierelasjoner: List<PdlForelderBarnRelasjon> = emptyList(),
        sivilstand: List<Sivilstand> = emptyList(),
        fullmakt: List<Fullmakt> = emptyList(),
        adressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
    ) =
        PdlPersonMedRelasjonerOgAdressebeskyttelse(familierelasjoner, sivilstand, fullmakt, adressebeskyttelse)
}
