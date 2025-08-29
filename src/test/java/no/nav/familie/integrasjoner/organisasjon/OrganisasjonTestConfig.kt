package no.nav.familie.integrasjoner.organisasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.HentOrganisasjonResponse
import no.nav.familie.integrasjoner.client.rest.Navn
import no.nav.familie.integrasjoner.client.rest.OrganisasjonRestClient
import no.nav.familie.kontrakter.felles.organisasjon.Gyldighetsperiode
import no.nav.familie.kontrakter.felles.organisasjon.OrganisasjonAdresse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
class OrganisasjonTestConfig {
    @Bean
    @Profile("mock-organisasjon")
    @Primary
    fun organisasjonSoapClientMock(): OrganisasjonRestClient {
        val organisasjonRestClient = mockk<OrganisasjonRestClient>()
        val organisasjon =
            HentOrganisasjonResponse(
                navn = Navn("Navn på bedrift"),
                adresse =
                    OrganisasjonAdresse(
                        type = "Forretningsadresse",
                        kommunenummer = "0301",
                        adresselinje1 = "Sannergata 2",
                        adresselinje2 = null,
                        adresselinje3 = null,
                        postnummer = "0557",
                        gyldighetsperiode = Gyldighetsperiode(fom = LocalDate.of(2007, 8, 23), tom = null),
                    ),
            )
        every { organisasjonRestClient.hentOrganisasjon(any()) } returns organisasjon

        return organisasjonRestClient
    }
}
