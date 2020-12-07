package no.nav.familie.integrasjoner.kodeverk

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.io.File
import java.io.IOException

@Configuration
class KodeverkTestConfig {


    @Bean
    @Profile("mock-kodeverk")
    @Primary
    fun kodeverkClientMock(): KodeverkClient {
        val kodeverkClient: KodeverkClient = mockk()
        every { kodeverkClient.hentPostnummer() } returns mockPostnummerRespons()
        every { kodeverkClient.ping() } returns Unit
        return kodeverkClient
    }

    private fun mockPostnummerRespons(): KodeverkDto {
        val postnummerResponseBody = File(getFile())
        return try {
            objectMapper.readValue(postnummerResponseBody)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av postnummerMock", e)
        }
    }

    private fun getFile(): String {
        return javaClass.classLoader?.getResource("kodeverk/postnummerrespons.json")?.file ?: error("Testkonfigurasjon feil")
    }
}
