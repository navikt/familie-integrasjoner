package no.nav.familie.integrasjoner.arbeidsfordeling

import io.mockk.every
import io.mockk.mockk
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class ArbeidsfordelingTestConfig {

    @Bean
    @Profile("mock-arbeidsfordeling")
    @Primary
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun arbeidsfordelingMock(): ArbeidsfordelingV1? {
        val arbeidsfordelingV1 = mockk<ArbeidsfordelingV1>(relaxed = true)

        every {
            arbeidsfordelingV1.finnBehandlendeEnhetListe(any())
        } returns FinnBehandlendeEnhetListeResponse().apply {
            behandlendeEnhetListe.apply {
                add(Organisasjonsenhet().apply {
                    enhetId = "4820"
                    enhetNavn = "NAV Familie- og pensjonsytelser Vadsø"
                })
            }
        }
        return arbeidsfordelingV1
    }
}