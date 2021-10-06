package no.nav.familie.integrasjoner.organisasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.soap.OrganisasjonSoapClient
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.Organisasjon
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.UstrukturertNavn
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonResponse
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.ValiderOrganisasjonResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class OrganisasjonTestConfig {

    @Bean
    @Profile("mock-organisasjon")
    @Primary
    fun organisasjonSoapClientMock(): OrganisasjonSoapClient {
        val organisasjonSoapClientMock: OrganisasjonSoapClient = mockk(relaxed = true)
        val organisasjon = HentOrganisasjonResponse().apply {
            organisasjon = Organisasjon().apply { navn = UstrukturertNavn() }
        }
        every { organisasjonSoapClientMock.hentOrganisasjon(any()) }
                .returns(organisasjon)

        val validerOrganisasjonRespons = ValiderOrganisasjonResponse().apply { isGyldigOrgnummer = true }
        every { organisasjonSoapClientMock.validerOrganisasjon(any()) } returns validerOrganisasjonRespons

        return organisasjonSoapClientMock
    }
}