package no.nav.familie.integrasjoner.aareg

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.aareg.domene.*
import no.nav.familie.integrasjoner.client.rest.AaregRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate
import kotlin.random.Random

@Configuration
class AaregTestConfig {

    @Bean
    @Profile("mock-aareg")
    @Primary
    fun AaregMockRestClient(): AaregRestClient {
        val klient: AaregRestClient = mockk(relaxed = true)

        var arbeidsforhold = Arbeidsforhold(
                navArbeidsforholdId = Random.Default.nextLong(),
                arbeidstaker = Arbeidstaker("Person", "01012012345", "2364077210183"),
                arbeidsgiver = Arbeidsgiver("Organisasjon", "998877665"),
                ansettelsesperiode = Ansettelsesperiode(Periode(fom = LocalDate.now().minusYears(1)))
        )

        every {
            klient.hentArbeidsforhold(any(), any())
        } returns listOf(arbeidsforhold)

        return klient
    }


}