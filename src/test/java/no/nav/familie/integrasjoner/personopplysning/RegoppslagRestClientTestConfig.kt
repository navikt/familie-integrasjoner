package no.nav.familie.integrasjoner.personopplysning

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.RegoppslagRestClient
import no.nav.familie.integrasjoner.personopplysning.internal.Adresse
import no.nav.familie.integrasjoner.personopplysning.internal.AdresseKildeCode
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class RegoppslagRestClientTestConfig {
    @Bean
    @Profile("mock-regoppslag")
    @Primary
    fun regoppslagRestClientMock(): RegoppslagRestClient {
        val klient: RegoppslagRestClient = mockk(relaxed = true)

        every {
            klient.hentPostadresse(any(), any())
        } returns
            PostadresseResponse(
                navn = "Kari Normann",
                adresse = Adresse(AdresseKildeCode.BOSTEDSADRESSE, PostadresseType.NORSKPOSTADRESSE, "", null, null, "", "", "", ""),
            )
        return klient
    }
}
