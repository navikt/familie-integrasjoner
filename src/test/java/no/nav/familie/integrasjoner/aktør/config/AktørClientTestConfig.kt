package no.nav.familie.integrasjoner.aktør.config

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.integrasjoner.aktør.domene.Aktør
import no.nav.familie.integrasjoner.aktør.domene.Ident
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class AktørClientTestConfig {

    @Bean
    @Profile("mock-aktor")
    @Primary
    fun aktørregisterClientMock(): AktørregisterRestClient {
        val aktørregisterClient = mockk<AktørregisterRestClient>(relaxed = true)
        val stringCaptor = slot<String>()
        every { aktørregisterClient.hentAktørId(capture(stringCaptor)) }.answers {
            val identArg = stringCaptor.captured
            AktørResponse().withAktør(identArg, Aktør().withIdenter(listOf(finnRiktigAktørId(identArg))))
        }
        every { aktørregisterClient.hentPersonIdent(capture(stringCaptor)) }.answers {
            val identArg = stringCaptor.captured
            AktørResponse().withAktør(identArg, Aktør().withIdenter(listOf(finnRiktigPersonIdent(identArg))))
        }
        return aktørregisterClient
    }

    private fun finnRiktigAktørId(personIdent: String): Ident {
        return when (personIdent) {
            SøknadTestdata.morPersonident -> Ident()
                    .withIdent(SøknadTestdata.morAktørId)
                    .withGjeldende(true)
            SøknadTestdata.barnPersonident -> Ident()
                    .withIdent(SøknadTestdata.barnAktørId)
                    .withGjeldende(true)
            SøknadTestdata.farPersonident -> Ident()
                    .withIdent(SøknadTestdata.farAktørId)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskMorPersonident -> Ident()
                    .withIdent(SøknadTestdata.utenlandskMorAktørId)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskBarnPersonident -> Ident()
                    .withIdent(SøknadTestdata.utenlandskBarnAktørId)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskFarPersonident -> Ident()
                    .withIdent(SøknadTestdata.utenlandskFarAktørId)
                    .withGjeldende(true)
            else -> Ident().withIdent("1000011111111").withGjeldende(true)
        }
    }

    private fun finnRiktigPersonIdent(aktørId: String): Ident {
        return when (aktørId) {
            SøknadTestdata.morAktørId -> Ident()
                    .withIdent(SøknadTestdata.morPersonident)
                    .withGjeldende(true)
            SøknadTestdata.barnAktørId -> Ident()
                    .withIdent(SøknadTestdata.barnPersonident)
                    .withGjeldende(true)
            SøknadTestdata.farAktørId -> Ident()
                    .withIdent(SøknadTestdata.farPersonident)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskMorAktørId -> Ident()
                    .withIdent(SøknadTestdata.utenlandskMorPersonident)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskBarnAktørId -> Ident()
                    .withIdent(SøknadTestdata.utenlandskBarnPersonident)
                    .withGjeldende(true)
            SøknadTestdata.utenlandskFarAktørId -> Ident()
                    .withIdent(SøknadTestdata.utenlandskFarPersonident)
                    .withGjeldende(true)
            else -> Ident().withIdent("10000111111").withGjeldende(true)
        }
    }
}