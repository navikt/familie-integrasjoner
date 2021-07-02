package no.nav.familie.integrasjoner.aareg

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.aareg.domene.Ansettelsesperiode
import no.nav.familie.integrasjoner.aareg.domene.Arbeidsforhold
import no.nav.familie.integrasjoner.aareg.domene.Arbeidsgiver
import no.nav.familie.integrasjoner.aareg.domene.ArbeidsgiverType
import no.nav.familie.integrasjoner.aareg.domene.Arbeidstaker
import no.nav.familie.integrasjoner.aareg.domene.Periode
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

        val arbeidsforhold = Arbeidsforhold(
                navArbeidsforholdId = Random.Default.nextLong(),
                arbeidstaker = Arbeidstaker("Person", "01012012345", "2364077210183"),
                arbeidsgiver = Arbeidsgiver(ArbeidsgiverType.Organisasjon, "998877665"),
                ansettelsesperiode = Ansettelsesperiode(Periode(fom = LocalDate.now().minusYears(1)))
        )

        every {
            klient.hentArbeidsforhold(any(), any())
        } returns listOf(arbeidsforhold)

        return klient
    }


}